package de.itemis.mosig.fluffy.tests.java.concurrency;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import de.itemis.mosig.fluffy.tests.java.exceptions.InstantiationNotPermittedException;

public final class FluffyLatches {

    private FluffyLatches() {
        throw new InstantiationNotPermittedException();
    }

    public static void assertLatch(CountDownLatch latch, Duration timeout) {
        requireNonNull(latch, "latch");
        requireNonNull(timeout, "timeout");

        boolean latchWasZero = waitOnLatch(latch, timeout);
        assertThat(latchWasZero).as("Waiting on latch to become zero timed out.").isTrue();
    }

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
