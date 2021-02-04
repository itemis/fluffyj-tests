package de.itemis.mosig.fluffy.tests.java.sneaky;

import static de.itemis.mosig.fluffy.tests.java.ExpectedExceptions.EXPECTED_CHECKED_EXCEPTION;
import static de.itemis.mosig.fluffy.tests.java.ExpectedExceptions.EXPECTED_UNCHECKED_EXCEPTION;
import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertIsStaticHelper;
import static de.itemis.mosig.fluffy.tests.java.sneaky.Sneaky.throwThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class SneakyTest {

    @Test
    public void is_static_helper() {
        assertIsStaticHelper(Sneaky.class);
    }

    @Test
    public void should_throw_unchecked_exception() {
        assertThatThrownBy(() -> throwThat(EXPECTED_UNCHECKED_EXCEPTION)).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void should_throw_same_unchecked_exception() {
        assertThatThrownBy(() -> throwThat(EXPECTED_UNCHECKED_EXCEPTION)).isSameAs(EXPECTED_UNCHECKED_EXCEPTION);
    }

    @Test
    public void should_throw_checked_exception() {
        assertThatThrownBy(() -> throwThat(EXPECTED_CHECKED_EXCEPTION)).isInstanceOf(Exception.class);
    }

    @Test
    public void should_throw_same_checked_exception() {
        assertThatThrownBy(() -> throwThat(EXPECTED_CHECKED_EXCEPTION)).isSameAs(EXPECTED_CHECKED_EXCEPTION);
    }

    @Test
    public void should_throw_throwable() {
        assertThatThrownBy(() -> throwThat(new Throwable())).isInstanceOf(Throwable.class);
    }

    @Test
    public void should_throw_same_throwable() {
        Throwable expectedThrowable = new Throwable();
        assertThatThrownBy(() -> throwThat(expectedThrowable)).isSameAs(expectedThrowable);
    }

    @Test
    public void sneaky_implementation_is_done_correctly() {
        assertThatThrownBy(() -> should_not_need_to_declare_thrown_exceptions());
    }

    /**
     * This "test" won't compile if Sneaky's implementation was done wrong.
     */
    public void should_not_need_to_declare_thrown_exceptions() {
        throwThat(EXPECTED_CHECKED_EXCEPTION);
    }
}
