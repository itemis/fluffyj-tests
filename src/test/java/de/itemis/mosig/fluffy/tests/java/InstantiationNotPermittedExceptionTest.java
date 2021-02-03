package de.itemis.mosig.fluffy.tests.java;

import static java.lang.reflect.Modifier.isFinal;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class InstantiationNotPermittedExceptionTest {

    @Test
    public void has_expected_message() {
        assertThat(new InstantiationNotPermittedException().getMessage()).isEqualTo("Instantiation of class not permitted.");
    }

    @Test
    public void class_is_declared_final() {
        assertThat(isFinal(InstantiationNotPermittedException.class.getModifiers())).as("Class must be declared final.").isTrue();
    }
}
