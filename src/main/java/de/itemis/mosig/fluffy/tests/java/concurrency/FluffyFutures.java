package de.itemis.mosig.fluffy.tests.java.concurrency;

import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyLatches.assertLatch;
import static de.itemis.mosig.fluffy.tests.java.sneaky.Sneaky.throwThat;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import de.itemis.mosig.fluffy.tests.java.exceptions.InstantiationNotPermittedException;

public final class FluffyFutures {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private FluffyFutures() {
        throw new InstantiationNotPermittedException();
    }

    public static Future<?> scheduleInterruptibleFuture(ExecutorService executor) {
        requireNonNull(executor, "executor");

        CountDownLatch latch = new CountDownLatch(1);
        Future<?> result = executor.submit(() -> {
            boolean firstLoop = true;
            while (!Thread.currentThread().isInterrupted()) {
                if (firstLoop) {
                    firstLoop = false;
                    latch.countDown();
                }
            }
        });

        assertLatch(latch, Duration.ofSeconds(5));

        return result;
    }

    public static Future<?> scheduleExceptionalFuture(ExecutorService executor, Throwable expectedThrowable) {
        requireNonNull(executor, "executor");
        requireNonNull(expectedThrowable, "expectedThrowable");

        return executor.submit(() -> {
            throwThat(expectedThrowable);
        });
    }

    public static <T> T waitOnFuture(Future<T> future, Duration timeout) {
        requireNonNull(future, "future");
        requireNonNull(timeout, "timeout");

        T result = null;
        try {
            result = future.get(timeout.toMillis(), MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Was interrupted while waiting on latch to become zero.", e);
        } catch (TimeoutException e) {
            throw new RuntimeException("Waiting on Future to return a value timed out", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new RuntimeException("While waiting on future to finish: Future threw exception.", cause);
        }

        return result;
    }

    public static NeverendingFuture scheduleNeverendingFuture(ExecutorService executor) {
        requireNonNull(executor, "executor");
        return new NeverendingFuture(executor);
    }

    public static final class NeverendingFuture {
        private final Future<?> future;
        private final AtomicBoolean mustNotStop = new AtomicBoolean(true);

        private NeverendingFuture(ExecutorService executor) {
            this.future = executor.submit(() -> {
                while (mustNotStop.get());
            });
        }

        public Future<?> getFuture() {
            return future;
        }

        public void stop() {
            mustNotStop.set(false);
            waitOnFuture(future, DEFAULT_TIMEOUT);
        }
    }
}
