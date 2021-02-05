package de.itemis.mosig.fluffy.tests.java;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import org.assertj.core.api.Condition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.itemis.mosig.fluffy.tests.java.exceptions.InstantiationNotPermittedException;

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
     * <li>The constructor is a no args constructor</li>
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
        internalAssertNonInstantiatable(clazz);
    }

    private static Optional<Throwable> internalAssertNonInstantiatable(Class<?> clazz) {
        requireNonNull(clazz, "clazz");

        Optional<Throwable> result = empty();
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
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                thrownException = e;
            }
            assertThat(thrownException).as("Encountered working private constructor.").isInstanceOf(InvocationTargetException.class);
            result = Optional.ofNullable(thrownException.getCause());
        }

        return result;
    }

    /**
     * <p>
     * Convenience method. Assert that the provided class is a valid static helper. This is the case
     * when:
     * <ul>
     * <li>The class is declared final.</li>
     * <li>The class does not have any non static methods, except for those inherited from
     * {@link Object}.</li>
     * <li>The class does not inherit from any abstract base class.</li>
     * <li>The class has exactly one private no args constructor which must throw an
     * {@link InstantiationNotPermittedException}.</li>
     * </ul>
     *
     * @param clazz
     */
    public static void assertIsStaticHelper(Class<?> clazz) {
        requireNonNull(clazz, "clazz");

        assertFinal(clazz);
        var exceptionThrownByConstructor = internalAssertNonInstantiatable(clazz);

        if (exceptionThrownByConstructor.isPresent()) {
            var exceptionThrownByConstructorValue = exceptionThrownByConstructor.get();
            assertThat(exceptionThrownByConstructorValue).as("Constructor threw the wrong kind of exception.")
                .isExactlyInstanceOf(InstantiationNotPermittedException.class);
        } else {
            fail("Encountered unexpected behavior: Instantiating " + clazz.getSimpleName() + " caused an "
                + InvocationTargetException.class.getSimpleName() + " with a null cause.");
        }


        List<Method> publicMethodCandidates =
            asList(clazz.getMethods()).stream().filter(method -> !method.getDeclaringClass().equals(Object.class)).collect(toList());
        assertThat(publicMethodCandidates).as("Static helper classes must not have any non static methods.")
            .allMatch(method -> isStatic(method.getModifiers()));

        assertThat(asList(clazz.getDeclaredMethods())).as("Static helper classes must not have any non static methods.")
            .allMatch(method -> isStatic(method.getModifiers()));
    }

    public static void assertSerialVersionUid(Class<?> clazz) {
        Condition<Field> serialVersionUID = new Condition<Field>(field -> {
            int modifiers = field.getModifiers();
            return isPrivate(modifiers) && isStatic(modifiers) && isFinal(modifiers) && field.getType() == long.class
                && "serialVersionUID".equals(field.getName());
        }, "private static final long serialVersionUID");

        assertThat(clazz.getDeclaredFields()).haveAtLeastOne(serialVersionUID);
    }
}
