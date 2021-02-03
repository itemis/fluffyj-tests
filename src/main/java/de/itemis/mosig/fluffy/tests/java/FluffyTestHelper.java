package de.itemis.mosig.fluffy.tests.java;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPrivate;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Condition;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class FluffyTestHelper {

    public static void assertFinal(Class<?> clazz) {
        requireNonNull(clazz, "clazz");
        assertThat(isFinal(clazz.getModifiers())).as("Class must be declared final.").isTrue();
    }

    /**
     * <p>
     * Assert that the provided {@code clazz} cannot be instantiated by any non hacky means.
     * </p>
     * <p>
     * A {@link Class} is considered non instantiatable if:
     * <ul>
     * <li>It has exactly one constructor.</li>
     * <li>The constructor is private.</li>
     * <li>The constructor throws a {@link Throwable} if it is invoked by reflection.</li>
     * </ul>
     * </p>
     * <p>
     * Types considered non instantiatable per definition are:
     * <ul>
     * <li>Abstract classes</li>
     * <li>Interfaces</li>
     * <li>Primitives</li>
     * <li>Primitive array types</li>
     * <li>The {@code void} primitive</li>
     * </ul>
     * </p>
     * <p>
     * If the presented {@link Class} has exactly one private no args constructor that cannot be
     * called by reflection or does not throw a throwable, the assertion will fail.
     * </p>
     *
     * @param clazz
     */
    public static void assertNotInstantiatable(Class<?> clazz) {
        requireNonNull(clazz, "clazz");

        boolean isAbstract = Modifier.isAbstract(clazz.getModifiers());

        if (!isAbstract) {
            var constructors = Arrays.asList(clazz.getDeclaredConstructors());
            var description = "exactly one constructor, private and without args";

            assertThat(constructors).as(description).hasSize(1);

            Constructor<?> constructor = constructors.get(0);
            Condition<Constructor<?>> privateNoArgsConstructor = new Condition<>(constr -> {
                return isPrivate(constr.getModifiers()) && constr.getParameters().length == 0;
            }, description);
            assertThat(constructor).as("Encountered invalid constructor.").is(privateNoArgsConstructor);

            constructor.setAccessible(true);
            Exception thrownException = null;
            try {
                constructor.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                thrownException = e;
            }
            assertThat(thrownException).as("Encountered working private constructor.").isInstanceOf(InvocationTargetException.class);
        }
    }
}
