package de.itemis.mosig.fluffyj.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

public class ChainedMockitoAnswerTest {

    private static final class FirstAnswer implements ChainedMockitoAnswer<String> {
        @Override
        public String answer(InvocationOnMock invocation) throws Throwable {
            return "first";
        }
    }

    private static final class SecondAnswer implements ChainedMockitoAnswer<String> {
        @Override
        public String answer(InvocationOnMock invocation) throws Throwable {
            return "second";
        }
    }

    @Test
    public void t() throws Throwable {
        assertThat(new FirstAnswer().andThen(new SecondAnswer()).answer(null)).isEqualTo("second");
    }
}
