package de.itemis.mosig.fluffy.tests.java;

import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertFinal;
import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertSerialVersionUid;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class InstantiationNotPermittedExceptionTest {

    @Test
    public void has_expected_message() {
        assertThat(new InstantiationNotPermittedException()).hasMessage("Instantiation of class not permitted.");
    }

    @Test
    public void class_is_declared_final() {
        assertFinal(InstantiationNotPermittedException.class);
    }

    @Test
    public void exception_is_unchecked() {
        assertThat(new InstantiationNotPermittedException()).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void has_serial_version_uid() {
        assertSerialVersionUid(InstantiationNotPermittedException.class);
    }
}
