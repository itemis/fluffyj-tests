package de.itemis.mosig.fluffy.tests.java.concurrency;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(FluffyThreadSafety.class)
public @interface AssertThreadSafety {
    int threadCount() default 2;
}
