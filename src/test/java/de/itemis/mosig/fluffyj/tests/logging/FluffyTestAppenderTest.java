package de.itemis.mosig.fluffyj.tests.logging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class FluffyTestAppenderTest {

    private static final Logger LOG = LoggerFactory.getLogger(FluffyTestAppenderTest.class);

    private static final Level EXPECTED_LEVEL = Level.INFO;
    private static final String EXPECTED_MSG = "expectedMsg";

    private ILoggingEvent logMsgMock;
    private ExtensionContext extensionCtxMock;

    private FluffyTestAppender underTest;

    @BeforeEach
    public void setUp() throws Exception {
        extensionCtxMock = mock(ExtensionContext.class);

        logMsgMock = mock(ILoggingEvent.class);
        when(logMsgMock.getLevel()).thenReturn(EXPECTED_LEVEL);
        when(logMsgMock.getFormattedMessage()).thenReturn(EXPECTED_MSG);

        underTest = new FluffyTestAppender();
        underTest.beforeEach(extensionCtxMock);
    }

    @AfterEach
    public void tearDown() throws Exception {
        underTest.afterEach(extensionCtxMock);
    }

    @Test
    public void append_appends() {
        assertFail(EXPECTED_LEVEL, EXPECTED_MSG);
        underTest.append(createLogMsg(EXPECTED_LEVEL, EXPECTED_MSG));
        assertSuccess(EXPECTED_LEVEL, EXPECTED_MSG);
    }

    @Test
    public void daAppend_appends() {
        assertFail(EXPECTED_LEVEL, EXPECTED_MSG);
        underTest.doAppend(createLogMsg(EXPECTED_LEVEL, EXPECTED_MSG));
        assertSuccess(EXPECTED_LEVEL, EXPECTED_MSG);
    }

    @Test
    public void log_messages_are_recorded() {
        assertFail(EXPECTED_LEVEL, EXPECTED_MSG);
        LOG.info(EXPECTED_MSG);
        assertSuccess(EXPECTED_LEVEL, EXPECTED_MSG);
    }

    @Test
    public void afterEach_deregisters_appender() throws Exception {
        underTest.afterEach(extensionCtxMock);
        LOG.info(EXPECTED_MSG);
        assertFail(EXPECTED_LEVEL, EXPECTED_MSG);
    }

    @Test
    public void asserts_message_parts() {
        var expectedMsg = "multi part message";

        LOG.info(expectedMsg);
        assertSuccess(EXPECTED_LEVEL, "part");
    }

    @Test
    public void assert_respects_log_levels() {
        LOG.error(EXPECTED_MSG);
        assertFail(Level.INFO, EXPECTED_MSG);
    }

    private void assertSuccess(Level expectedLevel, String expectedMessage) {
        Assertions.assertDoesNotThrow(() -> underTest.assertLogContains(expectedLevel, expectedMessage));
    }

    private void assertFail(Level expectedLevel, String expectedMessage) {
        assertThatThrownBy(() -> underTest.assertLogContains(expectedLevel, expectedMessage)).isInstanceOf(AssertionError.class);
    }

    private ILoggingEvent createLogMsg(Level level, String msg) {
        var result = mock(ILoggingEvent.class);
        when(result.getLevel()).thenReturn(level);
        when(result.getFormattedMessage()).thenReturn(msg);

        return result;

    }
}
