package de.itemis.mosig.fluffy.tests.java;

import java.util.Properties;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * <p>
 * JUnit5 based extension that can be used to store all JVM system properties before a test method
 * is run and automatically restore them when it is done.
 * </p>
 * <p>
 * <b>Be aware:</b> Due to the global nature of JVM system properties, using this extensions when
 * running multiple tests in parallel in the same JVM could be dangerous, because changing system
 * properties could interfere with other tests. Thus, make sure to run tests using this extension
 * isolated within an exclusive JVM.
 * </p>
 */
public final class FluffySystemProperties implements BeforeEachCallback, AfterEachCallback {

    private final Properties oldProps = new Properties();

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        synchronized (oldProps) {
            if (oldProps.isEmpty()) {
                oldProps.putAll(System.getProperties());
            }
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        System.setProperties(oldProps);
    }
}
