package de.itemis.mosig.fluffy.tests.java;

import static java.util.Objects.requireNonNull;

import org.mockito.stubbing.Answer;

import java.util.concurrent.Callable;

import de.itemis.mosig.fluffy.tests.java.exceptions.InstantiationNotPermittedException;

/**
 * Convenience methods to create 'answers' that may be used by mocking frameworks. The goal here is
 * to make mocking code more readable.
 */
public final class FluffyAnswers {

    private FluffyAnswers() {
        throw new InstantiationNotPermittedException();
    }

    /**
     * <p>
     * Create a new {@link ChainedMockitoAnswer} that is always going to throw an exception if
     * {@link Answer#answer(org.mockito.invocation.InvocationOnMock)} is invoked.
     * </p>
     * <p>
     * May be used to test behavior in cases where code throws specified exceptions.
     * </p>
     *
     * @param expectedThrowable - This will be thrown.
     * @return A new instance of {@link ChainedMockitoAnswer}.
     */
    public static ChainedMockitoAnswer<Void> exceptionalAnswer(Throwable expectedThrowable) {
        requireNonNull(expectedThrowable, "expectedThrowable");

        return invocation -> {
            throw expectedThrowable;
        };
    }

    public static <T> ChainedMockitoAnswer<T> execute(Callable<T> code) {
        return invocation -> {
            T result = null;
            try {
                result = code.call();
            } catch (Throwable t) {
                throw new AssertionError("Encountered error while executing an Answer.", t);
            }

            return result;
        };
    }
}
