package de.itemis.mosig.fluffy.tests.java.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class FluffyExecutors {

    public static boolean kill(ExecutorService executor) {
        executor.shutdownNow();
        boolean result = false;
        try {
            result = executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result;
    }
}
