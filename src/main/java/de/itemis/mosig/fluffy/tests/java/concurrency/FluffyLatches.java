package de.itemis.mosig.fluffy.tests.java.concurrency;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import de.itemis.mosig.fluffy.tests.java.exceptions.InstantiationNotPermittedException;

/**
 * Convenience methods to work with {@link CountDownLatch latches}.
 */
public final class FluffyLatches {

    private FluffyLatches() {
        throw new InstantiationNotPermittedException();
    }

    /**
     *
     * @param latch
     * @param timeout
     */
    public static void assertLatch(CountDownLatch latch, Duration timeout) {
        requireNonNull(latch, "latch");
        requireNonNull(timeout, "timeout");

        boolean latchWasZero = waitOnLatch(latch, timeout);
        assertThat(latchWasZero).as("Waiting on latch to become zero timed out.").isTrue();
    }

    /**
     * <p>
     * Wait on the provided {@code latch} to reach zero. Waits for a maximum of {@code timeout}
     * time. Precision is milliseconds.
     * </p>
     * <p>
     * If this method returns, the provided {@code latch} is guaranteed to be zero.
     * </p>
     * <p>
     * If waiting is interrupted, the method preserves the interrupt flag and throws a
     * {@link RuntimeException}.
     * </p>
     *
     * @param latch - Wait on this latch.
     * @param timeout - Wait for a maximum of this time. Precision is milliseconds.
     * @return {@code true} if count has reached zero. {@code false} otherwise.
     * @throws RuntimeException If the waiting thread is interrupted.
     */
    public static boolean waitOnLatch(CountDownLatch latch, Duration timeout) {
        requireNonNull(latch, "latch");
        requireNonNull(timeout, "timeout");

        boolean result = false;
        try {
            result = latch.await(timeout.toMillis(), MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Was interrupted while waiting on latch to become zero.", e);
        }

        return result;
    }
}
