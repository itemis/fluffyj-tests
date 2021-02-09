package de.itemis.mosig.fluffy.tests.java.concurrency;

import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertIsStaticHelper;
import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyExecutors.kill;
import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyFutures.scheduleInterruptibleFuture;
import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyFutures.scheduleNeverendingFuture;
import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyLatches.assertLatch;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FluffyExecutorsTest {

    private static final int DEFAULT_TIMEOUT_SECONDS = 5;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS);
    private static final int THREAD_COUNT = 2;

    private ExecutorService executor;
    private ExecutorService anotherExecutor;

    @BeforeEach
    public void setUp() {
        executor = newFixedThreadPool(THREAD_COUNT);
        anotherExecutor = newFixedThreadPool(THREAD_COUNT);
    }

    @AfterEach
    public void tearDown() {
        executor.shutdownNow();
        anotherExecutor.shutdownNow();
        boolean done = false;

        try {
            done = executor.awaitTermination(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                && anotherExecutor.awaitTermination(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            done = false;
        }

        if (!done) {
            System.err.println("[WARNING] Possible resource leak: Executor could possibly not terminate all tasks.");
        }
    }

    @Test
    public void is_static_helper() {
        assertIsStaticHelper(FluffyExecutors.class);
    }

    @Test
    public void executor_is_shutdown_after_kill() {
        kill(executor);
        assertThat(executor.isShutdown()).as("Executor was not shutdown.").isTrue();
    }

    @Test
    public void kill_cancels_all_running_tasks_and_returns_true() {
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            futures.add(scheduleInterruptibleFuture(executor));
        }

        assertThat(kill(executor)).as("Method must return true if all tasks have been canceled in time.").isTrue();
        assertThat(futures).as("Kill on executor did not stop running future.").allSatisfy(future -> future.isDone());
    }

    @Test
    public void kill_returns_false_if_not_all_tasks_could_be_canceled() {
        var neverendingFuture = scheduleNeverendingFuture(executor);
        try {
            assertThat(kill(executor)).as("kill must return false if executor could not end all tasks.").isFalse();
        } finally {
            neverendingFuture.stop();
        }
    }

    @Test
    public void if_kill_is_interrupted_it_returns_false_and_preserves_interrupted_flag() {
        var neverendingFuture = scheduleNeverendingFuture(executor);
        var latch = new CountDownLatch(1);
        AtomicBoolean killReturnValue = new AtomicBoolean(false);
        AtomicBoolean interruptFlagSet = new AtomicBoolean(false);

        anotherExecutor.submit(() -> {
            latch.countDown();
            killReturnValue.set(kill(executor));
            interruptFlagSet.set(Thread.currentThread().isInterrupted());
        });

        try {
            assertLatch(latch, DEFAULT_TIMEOUT);
            kill(anotherExecutor);

            assertThat(killReturnValue).as("kill must return false if interrupted.").isFalse();
            assertThat(interruptFlagSet).as("kill must preserve interrupted flag if interrupted.").isTrue();
        } finally {
            neverendingFuture.stop();
        }
    }
}
