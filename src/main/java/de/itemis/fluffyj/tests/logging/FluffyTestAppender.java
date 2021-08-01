package de.itemis.fluffyj.tests.logging;


import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * <p>
 * A JUnit5 extension for asserting SLF4J log contents. Requires SLF4J and Logback.
 * </p>
 * <p>
 * It attaches itself as an appender before each test method and detaches itself afterwards via the
 * AfterEach hook. Thus it will also detach itself even if the test itself threw an exception.
 * </p>
 * <p>
 * Usage:
 *
 * <pre>
 * public class SomeTest {
 *     // Visibility must at least be package private
 *     &#64;RegisterExtension
       FluffyTestAppender logAssert = new FluffyTestAppender();

       &#64;Test
       public void testSomething() {
           someApi.someSideEffect();
           logAssert.assertLogContains(..);
       }
 * }
 * </pre>
 * </p>
 */
public class FluffyTestAppender extends AppenderBase<ILoggingEvent> implements BeforeEachCallback, AfterEachCallback {
    private static final String LOGBACK_ROOT_LOGGER_NAME = ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME;
    private final List<LogEntry> logEntries = new ArrayList<>();

    @Override
    public void doAppend(ILoggingEvent e) {
        internalAppend(e);
    }

    @Override
    protected void append(ILoggingEvent e) {
        internalAppend(e);
    }

    /**
     * <p>
     * Assert that the log contains the provided {@code logMsg} with the provided {@code logLevel}.
     * </p>
     * <p>
     * Asserting does also work on parts of a message, i. e. if
     *
     * <pre>
     * WARN - I am a very fluffy log message
     * </pre>
     *
     * then assert("WARN","very fluffy") will pass.
     * </p>
     */
    public void assertLogContains(Level logLevel, String logMsg) {
        requireNonNull(logLevel, "logLevel");
        requireNonNull(logMsg, "logMsg");

        var matchFound = false;
        synchronized (logEntries) {
            matchFound = logEntries.stream().anyMatch(logEntry -> logEntry.logLevel.equals(logLevel) && logEntry.logMsg.contains(logMsg));
        }

        assertThat(matchFound).describedAs("Not found in log: [" + logLevel + "] " + logMsg).isTrue();
    }

    private void internalAppend(ILoggingEvent event) {
        var logLevel = event.getLevel();
        var logMsg = event.getFormattedMessage();
        synchronized (logEntries) {
            logEntries.add(new LogEntry(logLevel, logMsg));
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        var logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();
        setContext(logCtx);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(LOGBACK_ROOT_LOGGER_NAME)).addAppender(this);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        synchronized (logEntries) {
            logEntries.clear();
        }
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(LOGBACK_ROOT_LOGGER_NAME)).detachAppender(this);
    }

    private static final class LogEntry {
        final Level logLevel;
        final String logMsg;

        public LogEntry(Level logLevel, String logMsg) {
            this.logLevel = logLevel;
            this.logMsg = logMsg;
        }
    }
}
