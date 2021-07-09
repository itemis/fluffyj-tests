package de.itemis.mosig.fluffy.tests.java.exceptions;

import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertFinal;
import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertNullArgNotAccepted;
import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertSerialVersionUid;
import static de.itemis.mosig.fluffy.tests.java.exceptions.ExpectedExceptions.EXPECTED_THROWABLE;
import static de.itemis.mosig.fluffy.tests.java.exceptions.ThrowablePrettyfier.pretty;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ImplementationProblemExceptionTest {

    @Test
    public void has_serialVersionUid() {
        assertSerialVersionUid(ImplementationProblemException.class);
    }

    @Test
    public void is_final() {
        assertFinal(ImplementationProblemException.class);
    }

    @Test
    public void default_constructor_sets_expected_message() {
        var underTest = new ImplementationProblemException();

        assertThat(underTest.getMessage()).isEqualTo("An implementation problem occurred.");
    }

    @Test
    public void constructor_with_throwable_sets_expected_message() {
        var expectedThrowable = EXPECTED_THROWABLE;
        var underTest = new ImplementationProblemException(expectedThrowable);

        assertThat(underTest.getMessage()).isEqualTo("An implementation problem occurred: " + pretty(expectedThrowable));
    }

    @Test
    public void constructor_with_throwable_does_not_accept_null() {
        assertNullArgNotAccepted(() -> new ImplementationProblemException(null), "cause");
    }

    @Test
    public void constructor_with_string_and_throwable_does_not_accept_null_string() {
        assertNullArgNotAccepted(() -> new ImplementationProblemException(null, EXPECTED_THROWABLE), "description");
    }

    @Test
    public void constructor_with_string_and_throwable_does_not_accept_null_throwable() {
        assertNullArgNotAccepted(() -> new ImplementationProblemException("description", null), "cause");
    }

    @Test
    public void constructor_with_string_and_throwable_sets_expected_message() {
        var expectedThrowable = EXPECTED_THROWABLE;
        var expectedDescription = "description";

        var underTest = new ImplementationProblemException(expectedDescription, expectedThrowable);

        assertThat(underTest.getMessage()).isEqualTo("An implementation problem occurred: " + expectedDescription + ": " + pretty(expectedThrowable));
    }
}
