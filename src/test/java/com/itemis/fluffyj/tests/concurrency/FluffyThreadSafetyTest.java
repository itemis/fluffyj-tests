package com.itemis.fluffyj.tests.concurrency;

import static com.itemis.fluffyj.concurrency.FluffyExecutors.kill;
import static com.itemis.fluffyj.sneaky.Sneaky.throwThat;
import static com.itemis.fluffyj.tests.FluffyTestAnswers.exceptionalAnswer;
import static com.itemis.fluffyj.tests.FluffyTestAnswers.execute;
import static com.itemis.fluffyj.tests.FluffyTestHelper.assertFinal;
import static com.itemis.fluffyj.tests.concurrency.FluffyTestLatches.assertLatch;
import static com.itemis.fluffyj.tests.exceptions.ExpectedExceptions.EXPECTED_MESSAGE;
import static com.itemis.fluffyj.tests.exceptions.ExpectedExceptions.EXPECTED_UNCHECKED_EXCEPTION;
import static java.util.Arrays.asList;
import static java.util.Collections.synchronizedSet;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

// We do want to chose the time mocks are initialized by ourselves, even if they are not used later
// on.
// This is because we value test readability more than resource savings.
@MockitoSettings(strictness = Strictness.LENIENT)
public class FluffyThreadSafetyTest {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final String TEST_METHOD_NO_ARGS_WITH_THREAD_COUNT = "test_method_no_args_with_thread_count";
    private static final String TEST_METHOD_NO_ARGS = "test_method_no_args";
    private static final int DEFAULT_THREAD_COUNT = 2;
    private static final int EXPECTED_THREAD_COUNT = 10;

    @Mock
    private ExtensionContext extensionContextMock;
    @Mock
    private ReflectiveInvocationContext<Method> invocationContextMock;
    @Mock
    private Invocation<Void> invocationMock;

    private final AtomicInteger methodNoArgsInvocationCount = new AtomicInteger(0);
    private final AtomicInteger methodWithArgsInvocationCount = new AtomicInteger(0);
    private final Set<String> threadNamesThatCalledMethod = synchronizedSet(new HashSet<>());
    private final AtomicBoolean stopMethod = new AtomicBoolean(false);
    private final CountDownLatch getTargetLatch = new CountDownLatch(1);

    private FluffyTestThreadSafety underTest;

    @BeforeEach
    public void setUp() {
        underTest = new FluffyTestThreadSafety();
    }

    @Test
    public void is_final() {
        assertFinal(FluffyTestThreadSafety.class);
    }

    @Test
    public void skip_if_target_is_empty() {
        targetWillBeEmpty();

        runCodeToTest();

        verify(invocationMock, times(1)).skip();
    }

    @Test
    public void call_skip_if_target_is_present() {
        setupExecutable(TEST_METHOD_NO_ARGS);

        runCodeToTest();

        verify(invocationMock, times(1)).skip();
    }

    @Test
    public void invokes_method_at_least_once() {
        setupExecutable(TEST_METHOD_NO_ARGS);

        runCodeToTest();

        assertThat(methodNoArgsInvocationCount).hasValueGreaterThan(0);
    }

    @Test
    public void passes_all_required_args_to_method() {
        setupExecutable("test_method_with_args", new Object(), "string", true);

        runCodeToTest();

        assertThat(methodWithArgsInvocationCount).hasValue(2);
    }

    @Test
    public void when_executable_is_not_accessible_assertion_error() {
        setupExecutable("test_method_inaccessible");

        assertThatThrownBy(
            () -> underTest.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock),
            "Running an inaccessible method must throw an AssertionError.")
                .isInstanceOf(AssertionError.class)
                .hasMessage("Encountered problems while running test in parallel. Look at suppressed exceptions.")
                .extracting(throwable -> asList(throwable.getSuppressed())).asInstanceOf(LIST)
                .hasSize(DEFAULT_THREAD_COUNT)
                .allMatch(suppressed -> {
                    assertThat(suppressed).isInstanceOf(AssertionError.class);
                    final var actualSuppressed = (AssertionError) suppressed;
                    assertThat(actualSuppressed).hasMessage("Cannot test thread safety: Method is not accessible.")
                        .hasCauseInstanceOf(IllegalAccessException.class);
                    return true;
                });
    }

    @Test
    public void when_executable_lists_wrong_param_types_assertion_error() {
        setupExecutable(TEST_METHOD_NO_ARGS);
        when(invocationContextMock.getArguments()).thenReturn(asList("superfluous arg"));

        assertThatThrownBy(
            () -> underTest.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock),
            "Running a method with wrong parameters must throw an AssertionError.")
                .isInstanceOf(AssertionError.class)
                .hasMessage("Encountered problems while running test in parallel. Look at suppressed exceptions.")
                .extracting(throwable -> asList(throwable.getSuppressed())).asInstanceOf(LIST)
                .hasSize(DEFAULT_THREAD_COUNT)
                .allMatch(suppressed -> {
                    assertThat(suppressed).isInstanceOf(AssertionError.class);
                    final var actualSuppressed = (AssertionError) suppressed;
                    assertThat(actualSuppressed).hasMessage("Cannot test thread safety: Method arguments are wrong.")
                        .hasCauseInstanceOf(IllegalArgumentException.class);
                    return true;
                });
    }

    @Test
    public void when_executable_throws_exception_assertion_error() {
        setupExecutable("test_method_exception");

        assertThatThrownBy(
            () -> underTest.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock),
            "If the method throws an exception, an AssertionError must be thrown.")
                .isInstanceOf(AssertionError.class)
                .hasMessage("Encountered problems while running test in parallel. Look at suppressed exceptions.")
                .extracting(throwable -> asList(throwable.getSuppressed())).asInstanceOf(LIST)
                .hasSize(DEFAULT_THREAD_COUNT)
                .allMatch(suppressed -> {
                    assertThat(suppressed).isInstanceOf(AssertionError.class);
                    final var actualSuppressed = (AssertionError) suppressed;
                    assertThat(actualSuppressed).hasMessage("Cannot test thread safety: Method threw an exception.")
                        .hasCause(EXPECTED_UNCHECKED_EXCEPTION);
                    return true;
                });
    }

    @Test
    public void when_executable_throws_assertion_error_then_this_error_gets_propagated() {
        setupExecutable("test_method_assertion_error");

        assertThatThrownBy(
            () -> underTest.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock),
            "If the method throws an AssertionError, then this error must be propagated.")
                .isInstanceOf(AssertionError.class).hasMessage(EXPECTED_MESSAGE).hasNoCause();
    }

    /**
     * Cannot use a Spy here. See:
     * https://github.com/mockito/mockito/issues/2026#issuecomment-759052596
     */
    @Test
    public void when_executable_throws_exception_initialize_error_assertion_error() throws Throwable {
        final Throwable expectedThrowable = new ExceptionInInitializerError("expected");
        final var realMethod = setupExecutable(TEST_METHOD_NO_ARGS);
        final var methodMock = mock(Method.class);
        when(methodMock.getDeclaredAnnotation(AssertThreadSafety.class))
            .thenReturn(realMethod.getDeclaredAnnotation(AssertThreadSafety.class));
        when(methodMock.invoke(this)).thenAnswer(exceptionalAnswer(expectedThrowable));
        when(invocationContextMock.getExecutable()).thenReturn(methodMock);

        assertThatThrownBy(
            () -> underTest.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock),
            "If an ExceptionInitializeError occurs, an AssertionError must be thrown.")
                .isInstanceOf(AssertionError.class)
                .hasMessage("Encountered problems while running test in parallel. Look at suppressed exceptions.")
                .extracting(throwable -> asList(throwable.getSuppressed())).asInstanceOf(LIST)
                .hasSize(DEFAULT_THREAD_COUNT)
                .allMatch(suppressed -> {
                    assertThat(suppressed).isInstanceOf(AssertionError.class);
                    final var actualSuppressed = (AssertionError) suppressed;
                    assertThat(actualSuppressed).hasMessage("Cannot test thread safety: Initialization failed.")
                        .hasCause(expectedThrowable);
                    return true;
                });
    }

    @Test
    public void no_thread_count_means_thread_count_2() {
        setupExecutable("test_method_no_args_no_thread_count");

        runCodeToTest();

        assertThat(methodNoArgsInvocationCount).hasValue(DEFAULT_THREAD_COUNT);
    }

    @Test
    public void when_thread_count_specified_then_thread_count_invocations() {
        setupExecutable(TEST_METHOD_NO_ARGS_WITH_THREAD_COUNT);

        runCodeToTest();

        assertThat(methodNoArgsInvocationCount).hasValue(EXPECTED_THREAD_COUNT);
    }

    @Test
    public void test_method_invocations_happen_in_their_respective_threads() {
        setupExecutable(TEST_METHOD_NO_ARGS_WITH_THREAD_COUNT);

        runCodeToTest();

        assertThat(threadNamesThatCalledMethod)
            .as("The method must have been called by " + EXPECTED_THREAD_COUNT + " different threads.")
            .hasSize(EXPECTED_THREAD_COUNT);
    }

    @Test
    public void when_method_invocation_thread_is_interrupted_then_interrupt_exception() {
        setupExecutable("test_method_runs_forever");
        final var executor = Executors.newFixedThreadPool(1);
        final var futureScheduledLatch = new CountDownLatch(1);
        final Future<?> future = executor.submit(() -> {
            futureScheduledLatch.countDown();
            runCodeToTest();
        });

        try {
            assertLatch(futureScheduledLatch, DEFAULT_TIMEOUT);
            assertLatch(getTargetLatch, DEFAULT_TIMEOUT);
            executor.shutdownNow();
            stopMethod.set(true);
            await().atMost(DEFAULT_TIMEOUT).untilAsserted(() -> {
                assertThat(future).failsWithin(DEFAULT_TIMEOUT).withThrowableOfType(ExecutionException.class)
                    .withCauseInstanceOf(InterruptedException.class);
            });
        } finally {
            stopMethod.set(true);
            kill(executor, DEFAULT_TIMEOUT);
        }
    }

    /*
     * ####################
     *
     * Start of helper code
     *
     * ####################
     */

    private void runCodeToTest() {
        try {
            underTest.interceptTestMethod(invocationMock, invocationContextMock, extensionContextMock);
        } catch (final Throwable e) {
            throwThat(e);
        }
    }

    @AssertThreadSafety
    public void test_method_assertion_error() {
        throw new AssertionError(EXPECTED_MESSAGE);
    }

    @AssertThreadSafety
    public void test_method_runs_forever() {
        while (!stopMethod.get() && !Thread.currentThread().isInterrupted()) {

        }
    }

    @AssertThreadSafety
    public void test_method_exception() {
        throw EXPECTED_UNCHECKED_EXCEPTION;
    }

    @AssertThreadSafety
    private void test_method_inaccessible() {

    }

    @AssertThreadSafety(threadCount = EXPECTED_THREAD_COUNT)
    public void test_method_no_args_with_thread_count() {
        methodNoArgsInvocationCount.incrementAndGet();
        threadNamesThatCalledMethod.add(Thread.currentThread().getName());
    }

    @AssertThreadSafety
    public void test_method_no_args_no_thread_count() {
        methodNoArgsInvocationCount.incrementAndGet();
    }

    @AssertThreadSafety
    public void test_method_no_args() {
        methodNoArgsInvocationCount.incrementAndGet();
    }

    @AssertThreadSafety
    public void test_method_with_args(final Object argOne, final String argTwo, final Boolean argThree) {
        methodWithArgsInvocationCount.incrementAndGet();
    }

    private void targetWillBeEmpty() {
        when(invocationContextMock.getTarget()).thenReturn(Optional.empty());
    }

    private Method setupExecutable(final String methodName, final Object... args) {
        Method method = null;
        final var parameterTypes = Stream.of(args).map(Object::getClass).collect(toList()).toArray(new Class<?>[] {});
        try {
            final var localMethod = this.getClass().getDeclaredMethod(methodName, parameterTypes);
            try {
                when(invocationMock.proceed()).thenAnswer(execute(() -> localMethod.invoke(this, args)));
            } catch (final Throwable t) {
                fail("This should never happen.", t);
            }
            method = localMethod;
        } catch (final NoSuchMethodException e) {
            fail("This test class does not declare the specified method.", e);
        }

        when(invocationContextMock.getTarget()).thenAnswer(invocation -> {
            getTargetLatch.countDown();
            return Optional.of(FluffyThreadSafetyTest.this);
        });
        when(invocationContextMock.getArguments()).thenReturn(asList(args));
        when(invocationContextMock.getExecutable()).thenReturn(method);

        return method;
    }
}
