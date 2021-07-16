package de.itemis.mosig.fluffy.tests.java;

import static de.itemis.mosig.fluffy.tests.java.concurrency.FluffyExecutors.kill;
import static de.itemis.mosig.fluffy.tests.java.sneaky.Sneaky.throwThat;
import static java.lang.Thread.currentThread;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Condition;

import de.itemis.mosig.fluffy.tests.java.exceptions.InstantiationNotPermittedException;

/**
 * Arbitrary convenience methods to make test code easier to read. In case of error they will
 * usually throw {@link AssertionError AssertionErrors} in order to better integrate with JUnit etc.
 * tests.
 */
public final class FluffyTestHelper {

    private FluffyTestHelper() {
        throw new InstantiationNotPermittedException();
    }

    /**
     * Assert that the provided {@code clazz} is declared {@code final}.
     *
     * @param clazz
     */
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

    /**
     * <p>
     * Assert that the provided {@code clazz} has at least one member with the following properties:
     * <ul>
     * <li>It is declared private.</li>
     * <li>It is declared static.</li>
     * <li>It is declared final.</li>
     * <li>Its type is long.</li>
     * <li>Its name is serialVersionUID</li>
     * </ul>
     * </p>
     *
     * @param clazz
     */
    public static void assertSerialVersionUid(Class<?> clazz) {
        requireNonNull(clazz, "clazz");

        Condition<Field> serialVersionUID = new Condition<Field>(field -> {
            int modifiers = field.getModifiers();
            return isPrivate(modifiers) && isStatic(modifiers) && isFinal(modifiers) && field.getType() == long.class
                && "serialVersionUID".equals(field.getName());
        }, "private static final long serialVersionUID");

        assertThat(clazz.getDeclaredFields()).haveAtLeastOne(serialVersionUID);
    }

    /**
     * Assert that {@code code} throws a {@link NullPointerException} with {@code argName} in its
     * message. May be used to test if a method properly handles null arguments.
     *
     * @param code The code to run.
     * @param argName Must be contained in the expected {@link NullPointerException}.
     */
    public static void assertNullArgNotAccepted(Runnable code, String argName) {
        requireNonNull(code, "argConsumer");
        requireNonNull(argName, "argName");

        assertThatThrownBy(() -> code.run()).as("NullPointerException is expected when methods encounter null for argument '" + argName + "'.")
            .isInstanceOf(NullPointerException.class).as("Argument name '" + argName + "' is missing in exception's message").hasMessageContaining(argName);
    }

    /**
     * Sleeps for the specified {@code waitingTime}. Thread interruptions are respected. In case of
     * interruption the thread interrupt flag will be preserved.
     *
     * @param waitingTime - Precision is milliseconds.
     * @throws InterruptedException - In case the sleeping thread is interrupted.
     */
    // Using an Executor based approach here in order to avoid "drift", i. e. sleeping shorter /
    // longer than specified.
    public static void sleep(Duration waitingTime) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        CountDownLatch latch = new CountDownLatch(1);
        ScheduledFuture<?> future = executorService.schedule(() -> latch.countDown(), waitingTime.toMillis(), TimeUnit.MILLISECONDS);

        try {
            future.get();
        } catch (InterruptedException e) {
            currentThread().interrupt();
            throwThat(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            fail("This should never happen.", cause);
        } finally {
            kill(executorService);
        }
    }
}
