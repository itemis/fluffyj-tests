package de.itemis.fluffyj.tests;

import static de.itemis.fluffyj.tests.FluffyTestAnswers.exceptionalAnswer;
import static de.itemis.fluffyj.tests.FluffyTestAnswers.execute;
import static de.itemis.fluffyj.tests.exceptions.ExpectedExceptions.EXPECTED_CHECKED_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

public class FluffyTestAnswersTest {

    private static final InvocationOnMock UNUSED_ARG = null;

    @Test
    public void is_static_helper() {
        FluffyTestHelper.assertIsStaticHelper(FluffyTestAnswers.class);
    }

    @Test
    public void exceptionalAnswer_returns_a_value() {
        assertThat(exceptionalAnswer(EXPECTED_CHECKED_EXCEPTION)).as("Method must not return null").isNotNull()
            .isInstanceOf(ChainedMockitoAnswer.class);
    }

    @Test
    public void exceptionalAnswer_always_throws_exception() {
        assertThatThrownBy(() -> exceptionalAnswer(EXPECTED_CHECKED_EXCEPTION).answer(UNUSED_ARG)).isSameAs(EXPECTED_CHECKED_EXCEPTION);
    }

    @Test
    public void execute_returns_a_value() {
        assertThat(execute(() -> {
            return null;
        })).as("Method must not return null").isNotNull()
            .isInstanceOf(ChainedMockitoAnswer.class);
    }

    @Test
    public void execute_returns_answer_that_executes_the_task() throws Throwable {
        String expectedValue = "expectedValue";
        var answer = execute(() -> {
            return expectedValue;
        });

        assertThat(answer.answer(UNUSED_ARG)).as("Created answer must execute provided code.").isEqualTo(expectedValue);
    }

    @Test
    public void execute_returns_answer_that_propagates_exceptions_as_assertion_error() {
        var answer = execute(() -> {
            throw EXPECTED_CHECKED_EXCEPTION;
        });

        assertThatThrownBy(() -> answer.answer(UNUSED_ARG), "Created answer must throw an AssertionError.").isInstanceOf(AssertionError.class)
            .hasMessage("Encountered error while executing an Answer.").hasCause(EXPECTED_CHECKED_EXCEPTION);
    }
}
