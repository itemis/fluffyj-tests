package de.itemis.mosig.fluffy.tests.java;

import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertNullArgNotAccepted;
import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertSerialVersionUid;
import static de.itemis.mosig.fluffy.tests.java.exceptions.ExpectedExceptions.EXPECTED_UNCHECKED_EXCEPTION;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.Serializable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FluffyTestHelperTest {

    // No serialVersionUID by intention
    @SuppressWarnings("serial")
    private static final class TestSerializableWithoutId implements Serializable {

    }

    private static final class TestSerializableWithId implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    @Test
    public void assert_serial_version_uid_fails_if_no_id() {
        assertThatThrownBy(() -> assertSerialVersionUid(TestSerializableWithoutId.class),
            "Types without serialVersionUID must fail the assertion.").isInstanceOf(AssertionError.class)
                .hasMessageContaining("private static final long serialVersionUID");
    }

    @Test
    public void assert_serial_version_uid_passes_if_id_exists() {
        assertDoesNotThrow(() -> assertSerialVersionUid(TestSerializableWithId.class));
    }

    @Test
    public void test_assert_null_arg_fail() {
        assertThatThrownBy(() -> assertNullArgNotAccepted(() -> nonNullSafeTestMethod(null), "arg"),
            "Methods that accept null arguments must fail the assertion.").isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expecting code to raise a throwable");
    }

    @Test
    public void test_assert_null_arg_success() {
        Assertions.assertDoesNotThrow(() -> assertNullArgNotAccepted(() -> nullSafeTestMethod(null), "arg"));
    }

    @Test
    public void test_assert_null_arg_with_wrong_exception_fails_assertion() {
        assertThatThrownBy(() -> assertNullArgNotAccepted(() -> {
            throw EXPECTED_UNCHECKED_EXCEPTION;
        }, "arg"),
            "Methods that do not throw NPE on null arguments must fail the assertion.").isInstanceOf(AssertionError.class)
                .hasMessageContaining("NullPointerException is expected when methods encounter null for argument 'arg'");
    }

    @Test
    public void test_assert_null_arg_with_wrong_exception_message_fails_assertion() {
        assertThatThrownBy(() -> assertNullArgNotAccepted(() -> {
            throw new NullPointerException();
        }, "arg"),
            "Methods that do not throw NPE with failed argument name must fail the assertion.").isInstanceOf(AssertionError.class)
                .hasMessageContaining("Argument name 'arg' is missing in exception's message");
    }

    @Test
    public void assert_null_arg_does_not_accept_nulls() {
        assertNullArgNotAccepted(() -> assertNullArgNotAccepted(null, "argConsumer"), "argConsumer");
        assertNullArgNotAccepted(() -> assertNullArgNotAccepted(() -> toString(), null), "argName");
    }

    private void nullSafeTestMethod(Object arg) {
        requireNonNull(arg, "arg");
    }

    private void nonNullSafeTestMethod(Object arg) {}
}
