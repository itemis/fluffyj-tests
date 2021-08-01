package com.itemis.fluffyj.tests;

import java.util.function.Function;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * <p>
 * This interface tries to mimic the 'chaining' behavior of functional interfaces like
 * {@link Function} with Mockito {@link Answer} instances.
 * </p>
 * <p>
 * Example:
 *
 * <pre>
 * when(mock.methodToMock).thenAnswers(answer1().andThen(answer2));
 * </pre>
 * </p>
 *
 * @param <RETURN_TYPE_OF_ANSWER> - The return type of the {@link #answer(InvocationOnMock)} method.
 *        If the {@link Answer} is not supposed to return a value, use {@link Void} here.
 */
public interface ChainedMockitoAnswer<RETURN_TYPE_OF_ANSWER> extends Answer<RETURN_TYPE_OF_ANSWER> {

    /**
     * <p>
     * Combine this answer with the provided {@code after}. The result is an answer that is going to
     * first execute itself and then the code in {@code after}.
     * </p>
     * <p>
     * <b>Be aware: Only the result of the last answer in the constructed chain will be
     * returned.</b>
     * </p>
     *
     * @param after - The 'next' answer to 'run'.
     * @return A new {@link Answer} instance which combines {@code this} and {@code after}.
     */
    default ChainedMockitoAnswer<RETURN_TYPE_OF_ANSWER> andThen(Answer<RETURN_TYPE_OF_ANSWER> after) {
        return invocation -> {
            answer(invocation);
            return after.answer(invocation);
        };
    }
}
