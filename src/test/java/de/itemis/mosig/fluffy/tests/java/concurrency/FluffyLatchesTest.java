package de.itemis.mosig.fluffy.tests.java.concurrency;

import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertIsStaticHelper;
import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyExecutors.kill;
import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyLatches.assertLatch;
import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyLatches.waitOnLatch;
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

public class FluffyLatchesTest {

    private static final int THREAD_COUNT = 1;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMillis(500);

    private ExecutorService executor;

    @BeforeEach
    public void setUp() {
        executor = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    @AfterEach
    public void tearDown() {
        kill(executor);
    }

    @Test
    public void is_static_helper() {
        assertIsStaticHelper(FluffyLatches.class);
    }

    @Test
    public void assert_latch_passes_if_latch_is_zero() {
        CountDownLatch latch = new CountDownLatch(0);
        assertDoesNotThrow(() -> assertLatch(latch, DEFAULT_TIMEOUT));
    }

    @Test
    public void assert_latch_fails_if_latch_is_non_zero() {
        CountDownLatch latch = new CountDownLatch(1);
        assertThatThrownBy(() -> assertLatch(latch, DEFAULT_TIMEOUT)).isInstanceOf(AssertionError.class)
            .hasMessageContaining("Waiting on latch to become zero timed out.");
    }

    @Test
    public void assert_latch_waits_timeout() {
        CountDownLatch latch = new CountDownLatch(1);
        Duration expectedDuration = Duration.ofSeconds(1);
        long startTime = System.currentTimeMillis();
        assertThatThrownBy(() -> assertLatch(latch, expectedDuration)).isInstanceOf(AssertionError.class);
        long endTime = System.currentTimeMillis();
        assertThat(endTime - startTime).as("Encountered unexpected assert waiting time.").isGreaterThanOrEqualTo(expectedDuration.toMillis());
    }

    @Test
    public void assert_fails_when_wait_is_interrupted_and_preserves_interrupt_flag() {
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch futureStartedLatch = new CountDownLatch(1);

        AtomicBoolean interruptedFlagPreserved = new AtomicBoolean(false);
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
            futureStartedLatch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Was interrupted while waiting on latch to become zero.", e);
        }

        kill(executor);
        assertThatThrownBy(() -> future.get(DEFAULT_TIMEOUT.toMillis(), MILLISECONDS)).isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class).getCause().hasMessageContaining("Was interrupted while waiting on latch to become zero.")
            .hasCauseExactlyInstanceOf(InterruptedException.class);

        assertThat(interruptedFlagPreserved).as("Interrupted assert does not preserve interrupt flag.").isTrue();
    }

    @Test
    public void waitOnLatch_passes_if_latch_is_zero() {
        CountDownLatch latch = new CountDownLatch(0);
        assertThat(waitOnLatch(latch, DEFAULT_TIMEOUT)).as("Encountered unexpected return value.").isTrue();
    }

    @Test
    public void waitOnLatch_fails_if_latch_is_non_zero() {
        CountDownLatch latch = new CountDownLatch(1);
        assertThat(waitOnLatch(latch, DEFAULT_TIMEOUT)).as("Encountered unexpected return value.").isFalse();
    }

    @Test
    public void waitOnLatch_waits_timeout() {
        CountDownLatch latch = new CountDownLatch(1);
        Duration expectedDuration = Duration.ofSeconds(1);
        long startTime = System.currentTimeMillis();
        waitOnLatch(latch, expectedDuration);
        long endTime = System.currentTimeMillis();
        assertThat(endTime - startTime).as("Encountered unexpected waiting time.").isGreaterThanOrEqualTo(expectedDuration.toMillis());
    }

    @Test
    public void waitOnLatch_fails_when_wait_is_interrupted_and_preserves_interrupt_flag() {
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch futureStartedLatch = new CountDownLatch(1);

        AtomicBoolean interruptedFlagPreserved = new AtomicBoolean(false);
        Future<?> future = executor.submit(() -> {
            futureStartedLatch.countDown();
            try {
                waitOnLatch(latch, DEFAULT_TIMEOUT);
            } catch (Throwable t) {
                interruptedFlagPreserved.set(Thread.currentThread().isInterrupted());
                throw t;
            }
        });

        try {
            futureStartedLatch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Was interrupted while waiting on latch to become zero.", e);
        }

        kill(executor);
        assertThatThrownBy(() -> future.get(DEFAULT_TIMEOUT.toMillis(), MILLISECONDS)).isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class).getCause().hasMessageContaining("Was interrupted while waiting on latch to become zero.")
            .hasCauseExactlyInstanceOf(InterruptedException.class);

        assertThat(interruptedFlagPreserved).as("Interrupted assert does not preserve interrupt flag.").isTrue();
    }
}
