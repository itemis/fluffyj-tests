package de.itemis.mosig.fluffyj.tests;

import static de.itemis.mosig.fluffyj.tests.FluffyTestHelper.assertNullArgNotAccepted;
import static de.itemis.mosig.fluffyj.tests.FluffyTestHelper.assertSerialVersionUid;
import static de.itemis.mosig.fluffyj.tests.FluffyTestHelper.sleep;
import static de.itemis.mosig.fluffyj.tests.concurrency.FluffyTestLatches.assertLatch;
import static de.itemis.mosig.fluffyj.tests.exceptions.ExpectedExceptions.EXPECTED_UNCHECKED_EXCEPTION;
import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.itemis.mosig.fluffyj.concurrency.FluffyExecutors;

public class FluffyTestHelperTest {

    private static final Duration EXPECTED_SLEEP_TIME = Duration.ofMillis(800);

    // No serialVersionUID by intention
    @SuppressWarnings("serial")
    private static final class TestSerializableWithoutId implements Serializable {

    }

    private static final class TestSerializableWithId implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    @Test
    public void assert_serial_version_uid_fails_if_no_id() {
        assertThatThrownBy(() -> assertSerialVersionUid(TestSerializableWithoutId.class),
            "Types without serialVersionUID must fail the assertion.").isInstanceOf(AssertionError.class)
                .hasMessageContaining("private static final long serialVersionUID");
    }

    @Test
    public void assert_serial_version_uid_passes_if_id_exists() {
        assertDoesNotThrow(() -> assertSerialVersionUid(TestSerializableWithId.class));
    }

    @Test
    public void test_assert_null_arg_fail() {
        assertThatThrownBy(() -> assertNullArgNotAccepted(() -> nonNullSafeTestMethod(null), "arg"),
            "Methods that accept null arguments must fail the assertion.").isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expecting code to raise a throwable");
    }

    @Test
    public void test_assert_null_arg_success() {
        Assertions.assertDoesNotThrow(() -> assertNullArgNotAccepted(() -> nullSafeTestMethod(null), "arg"));
    }

    @Test
    public void test_assert_null_arg_with_wrong_exception_fails_assertion() {
        assertThatThrownBy(() -> assertNullArgNotAccepted(() -> {
            throw EXPECTED_UNCHECKED_EXCEPTION;
        }, "arg"),
            "Methods that do not throw NPE on null arguments must fail the assertion.").isInstanceOf(AssertionError.class)
                .hasMessageContaining("NullPointerException is expected when methods encounter null for argument 'arg'");
    }

    @Test
    public void test_assert_null_arg_with_wrong_exception_message_fails_assertion() {
        assertThatThrownBy(() -> assertNullArgNotAccepted(() -> {
            throw new NullPointerException();
        }, "arg"),
            "Methods that do not throw NPE with failed argument name must fail the assertion.").isInstanceOf(AssertionError.class)
                .hasMessageContaining("Argument name 'arg' is missing in exception's message");
    }

    @Test
    public void assert_null_arg_does_not_accept_nulls() {
        assertNullArgNotAccepted(() -> assertNullArgNotAccepted(null, "argConsumer"), "argConsumer");
        assertNullArgNotAccepted(() -> assertNullArgNotAccepted(() -> toString(), null), "argName");
    }

    @Test
    public void sleep_sleeps_for_the_specified_time() {
        long startMillis = System.currentTimeMillis();
        sleep(EXPECTED_SLEEP_TIME);
        long stopMillis = System.currentTimeMillis();

        assertThat(stopMillis - startMillis).as("Method did not sleep long enough.").isGreaterThanOrEqualTo(EXPECTED_SLEEP_TIME.toMillis());
    }

    @Test
    public void sleep_is_interruptible_and_preserves_interrupt_flag() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean interruptFlagSet = new AtomicBoolean(false);
        Future<?> future = executor.submit(() -> {
            latch.countDown();
            try {
                sleep(EXPECTED_SLEEP_TIME);
            } catch (Throwable t) {
                interruptFlagSet.set(currentThread().isInterrupted());
                throw t;
            }
            return null;
        });

        assertLatch(latch, EXPECTED_SLEEP_TIME);
        Thread.sleep(200);
        FluffyExecutors.kill(executor, EXPECTED_SLEEP_TIME);

        assertThat(future).failsWithin(EXPECTED_SLEEP_TIME).withThrowableOfType(ExecutionException.class).withCauseInstanceOf(InterruptedException.class);
        assertThat(interruptFlagSet).as("In case sleep is interrupted, the interrupt flag must be preserved.").isTrue();
    }

    private void nullSafeTestMethod(Object arg) {
        requireNonNull(arg, "arg");
    }

    private void nonNullSafeTestMethod(Object arg) {}
}
