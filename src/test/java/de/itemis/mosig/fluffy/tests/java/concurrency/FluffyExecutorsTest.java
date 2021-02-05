package de.itemis.mosig.fluffy.tests.java.concurrency;

import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyExecutors.kill;
import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyFutures.scheduleNeverendingFuture;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class FluffyExecutorsTest {

    private static final int DEFAULT_TIMEOUT_SECONDS = 5;
    private static final int THREAD_COUNT = 1;

    private ExecutorService executor;

    @BeforeEach
    public void setUp() {
        executor = newFixedThreadPool(THREAD_COUNT);
    }

    @AfterEach
    public void tearDown() {
        executor.shutdownNow();
        try {
            executor.awaitTermination(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("[WARNING] Possible resource leak: Executor did not terminate in time.");
        }
    }

    @Test
    public void executor_is_shutdown_after_kill() {
        kill(executor);
        assertThat(executor.isShutdown()).as("Executor was not shutdown.").isTrue();
    }

    @Test
    public void kill_cancels_running_tasks() {
        for (int i = 0; i < 1000; i++) {
            Future<?> future = scheduleNeverendingFuture(executor);
            kill(executor);
            assertThat(future).as("Kill on executor did not stop running future.").isDone();
            executor = Executors.newFixedThreadPool(1);
        }
    }
}
