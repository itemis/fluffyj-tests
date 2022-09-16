package com.itemis.fluffyj.tests.concurrency;

import static com.itemis.fluffyj.concurrency.FluffyExecutors.kill;
import static com.itemis.fluffyj.tests.FluffyTestHelper.assertIsStaticHelper;
import static com.itemis.fluffyj.tests.concurrency.FluffyTestLatches.assertLatch;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FluffyTestLatchesTest {

    private static final int THREAD_COUNT = 1;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMillis(500);

    private ExecutorService executor;

    @BeforeEach
    public void setUp() {
        executor = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    @AfterEach
    public void tearDown() {
        kill(executor, DEFAULT_TIMEOUT);
    }

    @Test
    public void is_static_helper() {
        assertIsStaticHelper(FluffyTestLatches.class);
    }

    @Test
    public void assert_latch_passes_if_latch_is_zero() {
        var latch = new CountDownLatch(0);
        assertDoesNotThrow(() -> assertLatch(latch, DEFAULT_TIMEOUT));
    }

    @Test
    public void assert_latch_fails_if_latch_is_non_zero() {
        var latch = new CountDownLatch(1);
        assertThatThrownBy(() -> assertLatch(latch, DEFAULT_TIMEOUT)).isInstanceOf(AssertionError.class)
            .hasMessageContaining("Waiting on latch to become zero timed out.");
    }

    @Test
    public void assert_latch_waits_timeout() {
        var latch = new CountDownLatch(1);
        var expectedDuration = Duration.ofSeconds(1);
        var startTime = System.currentTimeMillis();
        assertThatThrownBy(() -> assertLatch(latch, expectedDuration)).isInstanceOf(AssertionError.class);
        var endTime = System.currentTimeMillis();
        assertThat(endTime - startTime).as("Encountered unexpected assert waiting time.").isGreaterThanOrEqualTo(expectedDuration.toMillis());
    }

    @Test
    public void assert_fails_when_wait_is_interrupted_and_preserves_interrupt_flag() {
        var latch = new CountDownLatch(1);
        var futureStartedLatch = new CountDownLatch(1);

        var interruptedFlagPreserved = new AtomicBoolean(false);
        Future<?> future = executor.submit(() -> {
            futureStartedLatch.countDown();
            try {
                assertLatch(latch, DEFAULT_TIMEOUT);
            } catch (Throwable t) {
                interruptedFlagPreserved.set(Thread.currentThread().isInterrupted());
                throw t;
            }
        });

        try {
            futureStartedLatch.await(DEFAULT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Was interrupted while waiting on latch to become zero.", e);
        }

        kill(executor, DEFAULT_TIMEOUT);
        assertThatThrownBy(() -> future.get(DEFAULT_TIMEOUT.toMillis(), MILLISECONDS))
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class).cause()
            .hasMessageContaining("Was interrupted while waiting on latch to become zero.")
            .hasCauseExactlyInstanceOf(InterruptedException.class);

        assertThat(interruptedFlagPreserved).as("Interrupted assert does not preserve interrupt flag.").isTrue();
    }
}
