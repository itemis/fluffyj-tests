package de.itemis.mosig.fluffy.tests.java.concurrency;

import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyLatches.assertLatch;
import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import de.itemis.mosig.fluffy.tests.java.exceptions.InstantiationNotPermittedException;

public final class FluffyFutures {

    private FluffyFutures() {
        throw new InstantiationNotPermittedException();
    }

    public static Future<?> scheduleNeverendingFuture(ExecutorService executor) {
        requireNonNull(executor, "executor");

        CountDownLatch latch = new CountDownLatch(1);
        Future<?> result = executor.submit(() -> {
            latch.countDown();
            while (!Thread.currentThread().isInterrupted());
        });

        assertLatch(latch, Duration.ofSeconds(5));

        return result;
    }

}
