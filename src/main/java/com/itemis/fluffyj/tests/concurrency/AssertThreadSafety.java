package com.itemis.fluffyj.tests.concurrency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <p>
 * May be used to test thread safety of some code.
 * </p>
 *
 * <p>
 * The idea here is:
 * <ol>
 * <li>Write a test that calls code that may not be thread safe.</li>
 * <li>Annotate the test with {@link AssertThreadSafety}.</li>
 * <li>Optionally specify {@code threadCount}. The more threads, the more likely to identify errors.
 * <li>Let JUnit5 run the test. Threads will be spawned and started at roughly the same time.</li>
 * <li>Each instance of the test is supposed to pass. If one instance failes while the others pass,
 * the tested code is likely to not be thread safe.</li>
 * </ol>
 * </p>
 * <p>
 * <b>Example: </b> Imagine you've got this code to test:
 *
 * <pre>
 * public class LazyInit {
 *     private String value;
 *
 *     public String getValue() {
 *         if (value == null) {
 *             value = UUID.randomUUID().toString();
 *         }
 *         return value;
 *     }
 * }
 * </pre>
 *
 * When running this code in parallel, it is likely that you won't get "your" value but this of
 * another thread, i. e. this test will eventually fail:
 *
 * <pre>
 * public class LazyInitTest {
 *     private LazyInit underTest = new LazyInit();
 *
 *     &#64;AssertThreadSafety(threadCount = 20)
 *     &#64;Test
 *     public void testLazy() throws Exception {
 *         // The returned value is the value set by the first thread that reached the value write
 *         // statement
 *         var firstValue = underTest.getValue();
 *         Thread.sleep(200);
 *         var secondValue = underTest.getValue();
 *
 *         assertThat(firstValue).isEqualTo(secondValue);
 *     }
 * }
 * </pre>
 *
 * Whereas the following LazyInit implementation will pass the test:
 *
 * <pre>
 * public class LazyInit {
 *     private String value;
 *
 *     public synchronized String getValue() {
 *         if (value == null) {
 *             value = UUID.randomUUID().toString();
 *         }
 *         return value;
 *     }
 * }
 * </pre>
 * </p>
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(FluffyTestThreadSafety.class)
public @interface AssertThreadSafety {
    int threadCount() default 2;
}
