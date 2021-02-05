package de.itemis.mosig.fluffy.tests.java.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ExecutorServiceHandle {

    private ExecutorService executorService;

    public ExecutorService getExecutor() {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(1);
        }

        return executorService;
    }

}
