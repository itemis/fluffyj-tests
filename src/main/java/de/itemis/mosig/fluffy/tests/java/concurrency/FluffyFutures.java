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

/**
 * Convenience methods to make working with {@link Future} more straight forward.
 */
public final class FluffyFutures {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private FluffyFutures() {
        throw new InstantiationNotPermittedException();
    }

    /**
     * <p>
     * Schedule a {@link Future} that runs forever until interrupted by a thread interrupt.
     * </p>
     * <p>
     * When this method returns, the returned {@link Future} is guaranteed to have been started.
     * </p>
     *
     * @param executor - Schedule with this {@link ExecutorService}.
     * @return A new instance of {@link Future} that does not return a value.
     */
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

    /**
     * <p>
     * Schedule a {@link Future} that will throw the provided {@code expectedThrowable}, i. e. when
     * calling {@link Future#get()} the future is guaranteed to throw an {@link ExecutionException}
     * that has the provided {@code expectedThrowable} set as cause.
     * </p>
     *
     * @param executor - Schedule on this executor.
     * @param expectedThrowable - The root cause to set.
     * @return A new instance of {@link Future} that does not return a value.
     */
    public static Future<?> scheduleExceptionalFuture(ExecutorService executor, Throwable expectedThrowable) {
        requireNonNull(executor, "executor");
        requireNonNull(expectedThrowable, "expectedThrowable");

        return executor.submit(() -> {
            throwThat(expectedThrowable);
        });
    }

    /**
     * <p>
     * Wait on the provided {@code future} for as long as {@code timeout} to return a value.
     * </p>
     * <p>
     * This method throws a {@link RuntimeException} if:
     * <ul>
     * <li>It is interrupted while waiting.</li>
     * <li>The timeout is reached.</li>
     * <li>The future did throw an exception.</li>
     * </ul>
     * </p>
     * <p>
     * If waiting is interrupted the thread's interrupt flag will be preserved.
     * </p>
     * <p>
     * If the future threw an exception, this exception will be attached to the
     * {@link RuntimeException} as cause instead of the {@link ExecutionException} that is thrown by
     * the Future's get methdod.
     * </p>
     *
     * @param <T> - The return type of the value produced by the provided {@code future}.
     * @param future - Wait on this {@link Future}.
     * @param timeout - Wait for as long as this duration. Precision is milliseconds.
     * @return The value produced by the {@code future} or {@code null} if none was produced.
     * @throws RuntimeException in case something goes wrong.
     */
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

    /**
     * <p>
     * Schedule a {@link Future} that never stops and is not interruptible by thread interrupts.
     * </p>
     * <p>
     * Because running non interruptible code may produce resource leaks, this method will return an
     * instance of {@link NeverendingFuture} which contains a method to manually shut down the
     * future when it is not required anymore. <b>Do not forget to manually shut it down when you
     * are done!</b>
     * </p>
     *
     * @param executor - Schedule on this executor.
     * @return A new instance of {@link NeverendingFuture}.
     */
    public static NeverendingFuture scheduleNeverendingFuture(ExecutorService executor) {
        requireNonNull(executor, "executor");
        return new NeverendingFuture(executor);
    }

    /**
     * Wraps a future that does never stop running and is not interruptible by thread interrupts.
     */
    public static final class NeverendingFuture {
        private final Future<?> future;
        private final AtomicBoolean mustNotStop = new AtomicBoolean(true);

        private NeverendingFuture(ExecutorService executor) {
            this.future = executor.submit(() -> {
                while (mustNotStop.get());
            });
        }

        /**
         * @return The {@link Future} wrapped by this instance.
         */
        public Future<?> getFuture() {
            return future;
        }

        /**
         * Stop the wrapped {@link Future}. When this method returns, the {@link Future} has stopped
         * executing.
         *
         * @throws RuntimeException In case waiting on the {@link Future} to stop times out. This
         *         should never happen.
         */
        public void stop() {
            mustNotStop.set(false);
            waitOnFuture(future, DEFAULT_TIMEOUT);
        }
    }
}
