package de.itemis.mosig.fluffy.tests.java.sneaky;

import static de.itemis.mosig.fluffy.tests.java.sneaky.Sneaky.throwThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class SneakyTest {

    @Test
    public void should_throw_unchecked_exception() {
        assertThatThrownBy(() -> throwThat(new RuntimeException())).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void should_throw_same_unchecked_exception() {
        RuntimeException expectedException = new RuntimeException();
        assertThatThrownBy(() -> throwThat(expectedException), "Encountered unexpected exception.").isSameAs(expectedException);
    }
}
