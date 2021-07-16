package de.itemis.mosig.fluffyj.tests.exceptions;

import static de.itemis.mosig.fluffyj.tests.FluffyTestHelper.assertIsStaticHelper;
import static de.itemis.mosig.fluffyj.tests.exceptions.ExpectedExceptions.EXPECTED_CHECKED_EXCEPTION;
import static de.itemis.mosig.fluffyj.tests.exceptions.ExpectedExceptions.EXPECTED_ERROR;
import static de.itemis.mosig.fluffyj.tests.exceptions.ExpectedExceptions.EXPECTED_ERROR_MESSAGE;
import static de.itemis.mosig.fluffyj.tests.exceptions.ExpectedExceptions.EXPECTED_MESSAGE;
import static de.itemis.mosig.fluffyj.tests.exceptions.ExpectedExceptions.EXPECTED_THROWABLE;
import static de.itemis.mosig.fluffyj.tests.exceptions.ExpectedExceptions.EXPECTED_THROWABLE_MESSAGE;
import static de.itemis.mosig.fluffyj.tests.exceptions.ExpectedExceptions.EXPECTED_UNCHECKED_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ExpectedExceptionsTest {

    @Test
    public void is_proper_static_helper() {
        assertIsStaticHelper(ExpectedExceptions.class);
    }

    @Test
    public void expected_runtime_exception_has_proper_message() {
        assertThat(EXPECTED_UNCHECKED_EXCEPTION).hasMessage(EXPECTED_MESSAGE);
    }

    @Test
    public void expected_unchecked_exception_is_runtime_exception() {
        assertThat(EXPECTED_UNCHECKED_EXCEPTION).isExactlyInstanceOf(RuntimeException.class);
    }

    @Test
    public void expected_checked_exception_has_proper_message() {
        assertThat(EXPECTED_CHECKED_EXCEPTION).hasMessage(EXPECTED_MESSAGE);
    }

    @Test
    public void expected_checked_exception_is_not_a_runtime_exception() {
        assertThat(EXPECTED_CHECKED_EXCEPTION).isNotInstanceOf(RuntimeException.class);
    }

    @Test
    public void expected_throwable_is_a_throwable() {
        assertThat(EXPECTED_THROWABLE).isExactlyInstanceOf(Throwable.class);
    }

    @Test
    public void expected_throwable_has_proper_message() {
        assertThat(EXPECTED_THROWABLE).hasMessage(EXPECTED_THROWABLE_MESSAGE);
    }

    @Test
    public void expected_error_is_an_error() {
        assertThat(EXPECTED_ERROR).isExactlyInstanceOf(Error.class);
    }

    @Test
    public void expected_error_has_proper_message() {
        assertThat(EXPECTED_ERROR).hasMessage(EXPECTED_ERROR_MESSAGE);
    }
}
