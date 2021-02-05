package de.itemis.mosig.fluffy.tests.java.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;

public class ExecutorServiceHandleTest {

    @Test
    public void get_executor_returns_executor() {
        assertThat(constructUnderTest().getExecutor()).isNotNull();
    }

    @Test
    public void calling_get_executor_twice_returns_same_executor_instance() {
        ExecutorServiceHandle underTest = constructUnderTest();
        ExecutorService firstResult = underTest.getExecutor();
        ExecutorService secondResult = underTest.getExecutor();
        assertThat(firstResult).isSameAs(secondResult);
    }

    @Test
    public void get_executor_is_thread_safe() {

    }

    private ExecutorServiceHandle constructUnderTest() {
        return new ExecutorServiceHandle();
    }
}
