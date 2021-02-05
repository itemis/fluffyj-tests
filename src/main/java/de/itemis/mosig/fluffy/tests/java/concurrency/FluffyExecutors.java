package de.itemis.mosig.fluffy.tests.java.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class FluffyExecutors {

    public static void kill(ExecutorService executor) {
        executor.shutdownNow();
        try {
            executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
