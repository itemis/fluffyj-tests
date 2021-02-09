package de.itemis.mosig.fluffy.tests.java.concurrency;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class ExecutorServiceHandle {

    private final int threadCount;
    private final ThreadFactory threadFactory;

    private volatile ExecutorService executorService;

    public ExecutorServiceHandle(int threadCount, String expectedName) {
        checkArgument(threadCount > 0, "Thread count must be gte 0.");
        requireNonNull(expectedName, "expectedName");

        this.threadCount = threadCount;
        this.threadFactory = task -> new Thread(task, expectedName);
    }

    public ExecutorService getExecutor() {
        if (executorService == null) {
            synchronized (this) {
                if (executorService == null) {
                    executorService = Executors.newFixedThreadPool(threadCount, threadFactory);
                }
            }
        }

        return executorService;
    }

    public boolean kill() {
        return FluffyExecutors.kill(getExecutor());
    }
}
