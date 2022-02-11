package com.itemis.fluffyj.tests.concurrency;

import static com.itemis.fluffyj.sneaky.Sneaky.throwThat;

import com.itemis.fluffyj.concurrency.ExecutorServiceHandle;
import com.itemis.fluffyj.concurrency.ThreadNameFactory;
import com.itemis.fluffyj.concurrency.UniqueShortIdThreadNameFactory;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * A Junit5 based extension that runs a test concurrently in multiple threads to see if it behaves
 * the same in all threads.
 *
 * @see AssertThreadSafety for details on how to use it.
 */
public final class FluffyTestThreadSafety implements InvocationInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(FluffyTestThreadSafety.class);

    private final ThreadNameFactory defaultThreadNameFactory = new UniqueShortIdThreadNameFactory(getClass().getSimpleName() + "-Thread");

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
            throws Throwable {
        invocation.skip();
        var target = invocationContext.getTarget();
        if (target.isPresent()) {
            Method method = invocationContext.getExecutable();
            AssertThreadSafety annotation = method.getDeclaredAnnotation(AssertThreadSafety.class);
            if (annotation != null) {
                int threadCount = annotation.threadCount();
                ExecutorServiceHandle executorHandle = new ExecutorServiceHandle(threadCount, defaultThreadNameFactory);
                ExecutorService executor = executorHandle.getExecutor();
                var targetValue = target.get();
                Object[] args = invocationContext.getArguments().toArray();
                var errorMessagPrefix = "Cannot test thread safety: ";
                CountDownLatch startAllThreadsLatch = new CountDownLatch(1);
                List<Future<?>> futures = new ArrayList<>();
                for (int i = 0; i < threadCount; i++) {
                    futures.add(executor.submit(() -> {
                        try {
                            startAllThreadsLatch.await();
                            method.invoke(targetValue, args);
                        } catch (IllegalAccessException e) {
                            fail(errorMessagPrefix + "Method is not accessible.", e);
                        } catch (IllegalArgumentException e) {
                            fail(errorMessagPrefix + "Method arguments are wrong.", e);
                        } catch (InvocationTargetException e) {
                            Throwable cause = e.getCause() == null ? e : e.getCause();
                            if (cause instanceof AssertionError) {
                                throwThat(cause);
                            } else {
                                fail(errorMessagPrefix + "Method threw an exception.", cause);
                            }
                        } catch (ExceptionInInitializerError e) {
                            fail(errorMessagPrefix + "Initialization failed.", e);
                        } catch (InterruptedException e) {
                            fail(errorMessagPrefix + "Method invocation was interrupted.", e);
                        }
                    }));
                }

                startAllThreadsLatch.countDown();
                AssertionError error = new AssertionError("Encountered problems while running test in parallel. Look at suppressed exceptions.");
                try {
                    for (var future : futures) {
                        try {
                            future.get();
                        } catch (ExecutionException e) {
                            Throwable cause = e.getCause() == null ? e : e.getCause();
                            if (cause instanceof CustomAssertionErrorMarker) {
                                error.addSuppressed(cause);
                            } else {
                                throw cause;
                            }
                        } catch (InterruptedException e) {
                            throwThat(e);
                        }
                    }

                    if (error.getSuppressed().length > 0) {
                        throw error;
                    }
                } finally {
                    if (!executorHandle.kill(Duration.ofMillis(500))) {
                        LOG.warn("Possible ressource leak. Could not kill executor in time. Some threads may still be running.");
                    }
                }
            }
        }
    }

    private void fail(String message, Throwable cause) {
        throw new CustomAssertionErrorMarker(message, cause);
    }

    private static final class CustomAssertionErrorMarker extends AssertionFailedError {
        private static final long serialVersionUID = 1L;

        public CustomAssertionErrorMarker(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
