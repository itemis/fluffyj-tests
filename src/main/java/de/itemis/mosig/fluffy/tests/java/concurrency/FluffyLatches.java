package de.itemis.mosig.fluffy.tests.java.concurrency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import de.itemis.mosig.fluffy.tests.java.exceptions.InstantiationNotPermittedException;

public final class FluffyLatches {

    private FluffyLatches() {
        throw new InstantiationNotPermittedException();
    }

    public static void assertLatch(CountDownLatch latch, Duration timeout) {
        boolean latchWasZero = false;
        try {
            latchWasZero = latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Was interrupted while waiting on latch to become zero.", e);
        }

        assertThat(latchWasZero).as("Waiting on latch to become zero timed out.").isTrue();
    }
}
