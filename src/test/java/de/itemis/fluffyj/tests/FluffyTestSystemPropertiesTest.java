package de.itemis.fluffyj.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

public class FluffyTestSystemPropertiesTest {

    private static final ExtensionContext EXTENSION_CTX_UNUSED = null;
    private static final String TEST_PROP_KEY = "testPropKey";
    private static final String TEST_PROP_VALUE = "testPropValue";
    private static final String SAFE_KNOWN_SYS_PROP = "java.vendor";

    private Properties oldProps;

    private FluffyTestSystemProperties underTest;

    @BeforeEach
    public void setUp() {
        oldProps = new Properties();
        oldProps.putAll(System.getProperties());

        underTest = new FluffyTestSystemProperties();
    }

    @AfterEach
    public void tearDown() {
        System.setProperties(oldProps);
    }

    @Test
    public void after_each_clears_new_props_() throws Exception {
        underTest.beforeEach(EXTENSION_CTX_UNUSED);
        System.setProperty(TEST_PROP_KEY, TEST_PROP_VALUE);
        underTest.afterEach(EXTENSION_CTX_UNUSED);

        assertThat(System.getProperty(TEST_PROP_KEY)).as("afterEach must clear additional properties").isNull();
    }

    @Test
    public void after_each_reverts_updated_existing_props_to_old_state() throws Exception {
        String oldVal = System.getProperty(SAFE_KNOWN_SYS_PROP);
        assertThat(oldVal).as("Unexpected: System property '" + SAFE_KNOWN_SYS_PROP + "' does not exist.").isNotNull();
        underTest.beforeEach(EXTENSION_CTX_UNUSED);
        System.setProperty(SAFE_KNOWN_SYS_PROP, TEST_PROP_VALUE);
        underTest.afterEach(EXTENSION_CTX_UNUSED);

        assertThat(System.getProperty(SAFE_KNOWN_SYS_PROP)).as("afterEach must revert updated existing properties to their old state.").isEqualTo(oldVal);
    }

    @Test
    public void after_each_restores_deleted_existing_props() throws Exception {
        String oldVal = System.getProperty(SAFE_KNOWN_SYS_PROP);
        assertThat(oldVal).as("Unexpected: System property '" + SAFE_KNOWN_SYS_PROP + "' does not exist.").isNotNull();
        underTest.beforeEach(EXTENSION_CTX_UNUSED);
        System.clearProperty(SAFE_KNOWN_SYS_PROP);
        underTest.afterEach(EXTENSION_CTX_UNUSED);

        assertThat(System.getProperty(SAFE_KNOWN_SYS_PROP)).as("afterEach must revert deleted existing properties to their old state.").isEqualTo(oldVal);
    }
}
