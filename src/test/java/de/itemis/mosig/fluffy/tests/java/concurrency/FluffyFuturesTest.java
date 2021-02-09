package de.itemis.mosig.fluffy.tests.java.concurrency;

import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertIsStaticHelper;
import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyExecutors.kill;
import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyFutures.scheduleExceptionalFuture;
import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyFutures.scheduleInterruptibleFuture;
import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyFutures.scheduleNeverendingFuture;
import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyFutures.waitOnFuture;
import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyLatches.waitOnLatch;
import static de.itemis.mosig.fluffy.tests.java.exceptions.ExpectedExceptions.EXPECTED_UNCHECKED_EXCEPTION;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import de.itemis.mosig.fluffy.tests.java.concurrency.FluffyFutures.NeverendingFuture;

public class FluffyFuturesTest {

    private static final int THREAD_COUNT = 1;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMillis(2000);
    private static final Duration EXTENDED_TIMEOUT = DEFAULT_TIMEOUT.plusSeconds(1);
    private ExecutorService executor;
    private ExecutorService anotherExecutor;

    @BeforeEach
    public void setUp() {
        setUpExecutors();
    }

    private void setUpExecutors() {
        executor = Executors.newFixedThreadPool(THREAD_COUNT);
        anotherExecutor = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    @AfterEach
    public void tearDown() {
        kill(executor);
        kill(anotherExecutor);
    }

    @Test
    public void is_static_helper() {
        assertIsStaticHelper(FluffyFutures.class);
    }

    @Test
    public void scheduleInterruptibleFuture_returns_future() {
        assertThat(scheduleInterruptibleFuture(executor)).isInstanceOf(Future.class);
    }

    @Test
    public void scheduleNeverendingFuture_returns_neverendingFuture() {
        assertThat(scheduleNeverendingFuture(executor)).isInstanceOf(NeverendingFuture.class);
    }

    @Test
    public void interruptibleFuture_is_interruptible() {
        Future<?> future = scheduleInterruptibleFuture(executor);
        kill(executor);
        assertThat(future).isDone();
    }

    @Test
    public void neverendingFuture_is_not_interruptible() {
        Future<?> future = scheduleNeverendingFuture(executor).getFuture();
        kill(executor);
        assertThat(future).isNotDone();
    }

    @Test
    public void neverendingFuture_is_done_when_stopped() {
        var futureWrapper = scheduleNeverendingFuture(executor);
        futureWrapper.stop();
        assertThat(futureWrapper.getFuture()).isDone();
    }

    @Test
    public void waitOnFuture_returns_value_when_future_is_done() {
        String expectedValue = "expectedValue";
        Future<String> future = executor.submit(() -> expectedValue);

        assertThat(waitOnFuture(future, DEFAULT_TIMEOUT)).as("Encountered unexpected return value.").isEqualTo(expectedValue);
    }

    @Test
    public void waitOnFuture_throws_runtimeException_with_timeoutException_if_no_value_after_timeout() {
        var future = scheduleInterruptibleFuture(executor);

        await().timeout(EXTENDED_TIMEOUT).catchUncaughtExceptions().alias("Asynchronous assertion").until(() -> {
            assertThatThrownBy(() -> waitOnFuture(future, DEFAULT_TIMEOUT), "Future interruption caused unexpected behavior.")
                .isInstanceOf(RuntimeException.class).hasMessage("Waiting on Future to return a value timed out")
                .hasRootCauseExactlyInstanceOf(TimeoutException.class);
            return true;
        });
    }

    @Test
    public void waitOnFuture_fails_when_wait_is_interrupted_and_preserves_interrupt_flag() {
        Future<?> neverendingFuture = scheduleInterruptibleFuture(executor);
        CountDownLatch waitingHasStartedLatch = new CountDownLatch(1);
        AtomicBoolean interruptedFlagPreserved = new AtomicBoolean(false);

        Future<?> futureToWaitOn = anotherExecutor.submit(() -> {
            waitingHasStartedLatch.countDown();
            try {
                waitOnFuture(neverendingFuture, DEFAULT_TIMEOUT);
            } catch (Throwable t) {
                interruptedFlagPreserved.set(Thread.currentThread().isInterrupted());
                throw t;
            }
        });

        waitOnLatch(waitingHasStartedLatch, DEFAULT_TIMEOUT);
        kill(anotherExecutor);

        assertThatThrownBy(() -> futureToWaitOn.get(DEFAULT_TIMEOUT.toMillis(), MILLISECONDS)).isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class).getCause().hasMessageContaining("Was interrupted while waiting on latch to become zero.")
            .hasCauseExactlyInstanceOf(InterruptedException.class);

        assertThat(interruptedFlagPreserved).as("Interrupted assert does not preserve interrupt flag.").isTrue();
    }

    @Test
    public void waitOnFuture_throws_runtimeException_if_executionException() {
        Future<?> future = scheduleExceptionalFuture(executor, EXPECTED_UNCHECKED_EXCEPTION);

        assertThatThrownBy(() -> waitOnFuture(future, DEFAULT_TIMEOUT)).isInstanceOf(RuntimeException.class)
            .hasMessage("While waiting on future to finish: Future threw exception.").hasCauseReference(EXPECTED_UNCHECKED_EXCEPTION);

    }

    @Test
    public void scheduleExceptionalFuture_schedules_a_future_that_throws_expected_exception() {
        Future<?> future = scheduleExceptionalFuture(executor, EXPECTED_UNCHECKED_EXCEPTION);

        assertThatThrownBy(() -> future.get(DEFAULT_TIMEOUT.getSeconds(), SECONDS), "Future did not throw expected exception.")
            .isInstanceOf(ExecutionException.class)
            .hasCauseReference(EXPECTED_UNCHECKED_EXCEPTION);
    }
}
