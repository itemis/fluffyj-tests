package de.itemis.fluffyj.tests;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.Callable;

import org.mockito.stubbing.Answer;

import de.itemis.fluffyj.exceptions.InstantiationNotPermittedException;

/**
 * Convenience methods to create 'answers' that may be used by mocking frameworks. The goal here is
 * to make mocking code more readable.
 */
public final class FluffyTestAnswers {

    private FluffyTestAnswers() {
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
