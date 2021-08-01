package com.itemis.fluffyj.tests.concurrency;

import static com.itemis.fluffyj.concurrency.FluffyExecutors.kill;
import static com.itemis.fluffyj.tests.FluffyTestHelper.assertIsStaticHelper;
import static com.itemis.fluffyj.tests.concurrency.FluffyTestFutures.scheduleExceptionalFuture;
import static com.itemis.fluffyj.tests.concurrency.FluffyTestFutures.scheduleInterruptibleFuture;
import static com.itemis.fluffyj.tests.concurrency.FluffyTestFutures.scheduleNeverendingFuture;
import static com.itemis.fluffyj.tests.exceptions.ExpectedExceptions.EXPECTED_UNCHECKED_EXCEPTION;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.itemis.fluffyj.tests.concurrency.FluffyTestFutures.NeverendingFuture;

public class FluffyTestFuturesTest {

    private static final int THREAD_COUNT = 1;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMillis(500);
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
        kill(executor, DEFAULT_TIMEOUT);
        kill(anotherExecutor, DEFAULT_TIMEOUT);
    }

    @Test
    public void is_static_helper() {
        assertIsStaticHelper(FluffyTestFutures.class);
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
        kill(executor, DEFAULT_TIMEOUT);
        assertThat(future).isDone();
    }

    @Test
    public void neverendingFuture_is_not_interruptible() {
        Future<?> future = scheduleNeverendingFuture(executor).getFuture();
        kill(executor, DEFAULT_TIMEOUT);
        assertThat(future).isNotDone();
    }

    @Test
    public void neverendingFuture_is_done_when_stopped() {
        var futureWrapper = scheduleNeverendingFuture(executor);
        futureWrapper.stop();
        assertThat(futureWrapper.getFuture()).isDone();
    }

    @Test
    public void scheduleExceptionalFuture_schedules_a_future_that_throws_expected_exception() {
        Future<?> future = scheduleExceptionalFuture(executor, EXPECTED_UNCHECKED_EXCEPTION);

        assertThatThrownBy(() -> future.get(DEFAULT_TIMEOUT.toMillis(), MILLISECONDS), "Future did not throw expected exception.")
            .isInstanceOf(ExecutionException.class)
            .hasCauseReference(EXPECTED_UNCHECKED_EXCEPTION);
    }
}
