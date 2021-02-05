package de.itemis.mosig.fluffy.tests.java.concurrency;

import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertIsStaticHelper;
import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyExecutors.kill;
import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyFutures.scheduleNeverendingFuture;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FluffyFuturesTest {

    private ExecutorService executor;

    @BeforeEach
    public void setUp() {
        executor = Executors.newFixedThreadPool(1);
    }

    @AfterEach
    public void tearDown() {
        kill(executor);
    }

    @Test
    public void is_static_helper() {
        assertIsStaticHelper(FluffyFutures.class);
    }

    @Test
    public void schedule_never_ending_future_returns_future() {
        assertThat(scheduleNeverendingFuture(executor)).isInstanceOf(Future.class);
    }

    @Test
    public void never_ending_future_is_interruptible() {
        for (int i = 0; i < 1000; i++) {
            Future<?> future = scheduleNeverendingFuture(executor);
            kill(executor);
            assertThat(future).isDone();
            executor = Executors.newFixedThreadPool(1);
        }
    }
}
