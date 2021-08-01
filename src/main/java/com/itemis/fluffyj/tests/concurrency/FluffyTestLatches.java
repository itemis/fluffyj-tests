package com.itemis.fluffyj.tests.concurrency;

import static com.itemis.fluffyj.concurrency.FluffyLatches.waitOnLatch;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import com.itemis.fluffyj.exceptions.InstantiationNotPermittedException;

/**
 * Convenience methods to work with {@link CountDownLatch latches}.
 */
public final class FluffyTestLatches {

    private FluffyTestLatches() {
        throw new InstantiationNotPermittedException();
    }

    /**
     * <p>
     * Wait on the provided {@code latch} to reach zero. Waits for a maximum of {@code timeout}
     * time. Precision is milliseconds.
     * </p>
     * <p>
     * If this method returns, the provided {@code latch} is guaranteed to be zero. Otherwise it
     * throws an {@link AssertionError}.
     * </p>
     * <p>
     * If waiting is interrupted, the method preserves the interrupt flag and throws a
     * {@link RuntimeException}.
     * </p>
     *
     * @param latch - Wait on this latch.
     * @param timeout - Wait for as long as timeout. Precision is milliseconds.
     * @throws RuntimeException If the waiting thread is interrupted.
     * @throws AssertionError If count did not reach zero within {@code timeout}.
     */
    public static void assertLatch(CountDownLatch latch, Duration timeout) {
        requireNonNull(latch, "latch");
        requireNonNull(timeout, "timeout");

        boolean latchWasZero = waitOnLatch(latch, timeout);
        assertThat(latchWasZero).as("Waiting on latch to become zero timed out.").isTrue();
    }
}
