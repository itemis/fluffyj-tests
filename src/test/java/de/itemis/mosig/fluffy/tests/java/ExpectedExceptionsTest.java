package de.itemis.mosig.fluffy.tests.java;

import static de.itemis.mosig.fluffy.tests.java.ExpectedExceptions.EXPECTED_CHECKED_EXCEPTION;
import static de.itemis.mosig.fluffy.tests.java.ExpectedExceptions.EXPECTED_UNCHECKED_EXCEPTION;
import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertIsStaticHelper;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ExpectedExceptionsTest {

    private final String expectedMessage = "Expected exception. Please ignore.";

    @Test
    public void is_proper_static_helper() {
        assertIsStaticHelper(ExpectedExceptions.class);
    }

    @Test
    public void expected_runtime_exception_has_proper_message() {
        assertThat(EXPECTED_UNCHECKED_EXCEPTION).hasMessage(expectedMessage);
    }

    @Test
    public void expected_unchecked_exception_is_runtime_exception() {
        assertThat(EXPECTED_UNCHECKED_EXCEPTION).isExactlyInstanceOf(RuntimeException.class);
    }

    @Test
    public void expected_checked_exception_has_proper_message() {
        assertThat(EXPECTED_CHECKED_EXCEPTION).hasMessage(expectedMessage);
    }

    @Test
    public void expected_checked_exception_is_not_a_runtime_exception() {
        assertThat(EXPECTED_CHECKED_EXCEPTION).isNotInstanceOf(RuntimeException.class);
    }

    @Test
    public void expected_message_has_proper_content() {
        assertThat(ExpectedExceptions.EXPECTED_MESSAGE).isEqualTo(expectedMessage);
    }
}
