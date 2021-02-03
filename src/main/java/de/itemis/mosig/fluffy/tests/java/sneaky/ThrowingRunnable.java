package de.itemis.mosig.fluffy.tests.java.sneaky;

import java.util.concurrent.Callable;

/**
 * A {@link Callable} that may throw a checked exception and does not return anything. Can be used
 * whenever a {@link Callable} is required but the code inside throws checked exceptions that we do
 * not want to take care of inside the callable, e. g:
 *
 * <pre>
 * // ugly
 * runner.run(() -> {
 *     try {
 *         doSomething();
 *     } catch (Exception e) {
 *         // error handling
 *     }
 *
 *     return null;
 * });
 *
 * // better
 * try
 *     runner.run(() -> doSomething());
 * } catch (Exception e) {
 *     // error handling
 * }
 *
 * // even better
 * runner.run(Sneaky.runnable(() -> doSomething()));
 * </pre>
 */
@FunctionalInterface
public interface ThrowingRunnable extends Callable<Void> {
    @Override
    default Void call() throws Exception {
        run();

        return null;
    }

    /**
     * Run this runnable / callable.
     *
     * @throws Exception in case something went wrong.
     */
    void run() throws Exception;
}