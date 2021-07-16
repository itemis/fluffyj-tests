package de.itemis.mosig.fluffy.tests.java.concurrency;

import java.util.function.Supplier;

/**
 * A {@link Supplier} that is supposed to create viable names for threads. The names should be
 * created in such a way so that threads can be easily distinguished from another. Implementations
 * are advised but not forced to provide unique ids. Implementors should document how likely it is
 * that different threads will end up with the same name.
 */
@FunctionalInterface
public interface ThreadNameFactory extends Supplier<String> {

    @Override
    default String get() {
        return generate();
    }

    /**
     * Convenience method to make the code more readable. {@link #get()} will always call this
     * method.
     *
     * @return A new thread name.
     */
    String generate();
}
