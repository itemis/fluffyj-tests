package de.itemis.mosig.fluffy.tests.java;

import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertSerialVersionUid;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import java.io.Serializable;

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
}
