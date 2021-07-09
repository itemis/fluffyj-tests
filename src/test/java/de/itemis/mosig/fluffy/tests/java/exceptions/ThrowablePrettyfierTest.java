package de.itemis.mosig.fluffy.tests.java.exceptions;

import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertFinal;
import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertIsStaticHelper;
import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertNullArgNotAccepted;
import static de.itemis.mosig.fluffy.tests.java.exceptions.ThrowablePrettyfier.pretty;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ThrowablePrettyfierTest {

    @Test
    public void is_final() {
        assertFinal(ThrowablePrettyfier.class);
    }

    @Test
    public void is_staticHelper() {
        assertIsStaticHelper(ThrowablePrettyfier.class);
    }

    @Test
    public void prettyfies_throwable_with_message() {
        var expectedException = ExpectedExceptions.EXPECTED_CHECKED_EXCEPTION;

        assertThat(pretty(expectedException)).isEqualTo(expectedException.getClass().getSimpleName() + ": " + expectedException.getMessage());
    }

    @Test
    public void pretty_does_not_accept_null() {
        assertNullArgNotAccepted(() -> pretty(null), "t");
    }

    @Test
    public void prettyfies_throwable_without_message() {
        var expectedException = new NullPointerException();

        assertThat(pretty(expectedException)).isEqualTo(expectedException.getClass().getSimpleName() + ": No further information");
    }
}
