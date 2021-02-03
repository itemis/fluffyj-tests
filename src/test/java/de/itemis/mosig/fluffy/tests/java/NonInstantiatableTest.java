package de.itemis.mosig.fluffy.tests.java;

import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertFinal;
import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertNotInstantiatable;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

public class NonInstantiatableTest {

    @Test
    public void final_class_passes_assertFinal() {
        assertDoesNotThrow(() -> assertFinal(FinalTestClass.class), "Final classes must pass the assertion.");
    }

    @Test
    public void non_final_class_fails_assertFinal() {
        assertThatThrownBy(() -> assertFinal(NonFinalTestClass.class), "Non final classes must not pass the assertion.").isInstanceOf(AssertionError.class);
    }

    @Test
    public void non_final_class_fails_assertFinal_with_proper_message() {
        assertThatThrownBy(() -> assertFinal(NonFinalTestClass.class), "Non final classes must not pass the assertion.")
            .hasMessageContaining("Class must be declared final.").isInstanceOf(AssertionError.class);
    }

    @Test
    public void assert_not_instantiatable_fails_at_public_contructor() {
        assertThatThrownBy(() -> assertNotInstantiatable(NonFinalTestClass.class)).isInstanceOf(AssertionError.class)
            .hasMessageContaining("Encountered invalid constructor.").hasMessageContaining("private");
    }

    @Test
    public void assert_not_instantiatable_fails_at_protected_constructor() {
        assertThatThrownBy(() -> assertNotInstantiatable(ProtectedConstructor.class)).isInstanceOf(AssertionError.class)
            .hasMessageContaining("Encountered invalid constructor.").hasMessageContaining("exactly one constructor, private and without args");
    }

    @Test
    public void assert_not_instantiatable_fails_at_package_private_constructor() {
        assertThatThrownBy(() -> assertNotInstantiatable(PackagePrivateConstructor.class)).isInstanceOf(AssertionError.class)
            .hasMessageContaining("Encountered invalid constructor.").hasMessageContaining("exactly one constructor, private and without args");
    }

    @Test
    public void assert_not_instantiatable_fails_at_working_private_constructor_with_args() {
        assertThatThrownBy(() -> assertNotInstantiatable(WorkingPrivateConstructorWithArgs.class)).isInstanceOf(AssertionError.class)
            .hasMessageContaining("Encountered invalid constructor.").hasMessageContaining("exactly one constructor, private and without args");
    }

    @Test
    public void assert_not_instantiatable_fails_at_multiple_constructors() {
        assertThatThrownBy(() -> assertNotInstantiatable(MultipleConstructors.class)).isInstanceOf(AssertionError.class)
            .hasMessageContaining("exactly one constructor, private and without args");
    }

    @Test
    public void assert_not_instantiatable_fails_at_working_private_no_args_constructor() {
        assertThatThrownBy(() -> assertNotInstantiatable(WorkingPrivateNoArgsConstructor.class)).isInstanceOf(AssertionError.class)
            .hasMessageContaining("Encountered working private constructor.");
    }

    @Test
    public void assert_not_instantiatable_passes_on_InvocationTargetException() {
        assertDoesNotThrow(() -> assertNotInstantiatable(ThrowingPrivateNoArgsConstructor.class),
            "A single throwing private no args constructor should pass the test.");
    }

    @Test
    public void assert_not_instantiatable_passes_on_abstract_class() {
        assertDoesNotThrow(() -> assertNotInstantiatable(AbstractTestClass.class),
            "Abstract classes must be non instantiatable per definition.");
    }

    @Test
    public void assert_not_instantiatable_passes_on_interface() {
        assertDoesNotThrow(() -> assertNotInstantiatable(TestInterface.class),
            "Interfaces must be non instantiatable per definition.");
    }

    @Test
    public void assert_not_instantiatable_passes_on_primitive() {
        assertDoesNotThrow(() -> assertNotInstantiatable(int.class),
            "Primitives must be non instantiatable per definition.");
    }

    @Test
    public void assert_not_instantiatable_passes_on_array() {
        assertDoesNotThrow(() -> assertNotInstantiatable(int[].class),
            "Primitive arrays must be non instantiatable per definition.");
    }

    @Test
    public void assert_not_instantiatable_fails_on_void() {
        assertDoesNotThrow(() -> assertNotInstantiatable(int[].class),
            "The void primitive must be non instantiatable per definition.");
    }

    public static final class FinalTestClass {

    }

    public static class NonFinalTestClass {
    }

    public static class ProtectedConstructor {
        protected ProtectedConstructor() {}
    }

    public static class PackagePrivateConstructor {
        PackagePrivateConstructor() {}
    }

    public static class WorkingPrivateConstructorWithArgs {
        private WorkingPrivateConstructorWithArgs(Object arg) {}
    }

    public static class MultipleConstructors {
        public MultipleConstructors() {}

        // Only accessed via reflection
        @SuppressWarnings("unused")
        private MultipleConstructors(Object arg) {}
    }

    public static class ThrowingPrivateNoArgsConstructor {
        private ThrowingPrivateNoArgsConstructor() {
            throw new RuntimeException("Expected exception");
        }
    }

    public static class WorkingPrivateNoArgsConstructor {
        private WorkingPrivateNoArgsConstructor() {}
    }

    public abstract class AbstractTestClass {
        private AbstractTestClass() {
            throw new RuntimeException("Expected exception");
        }
    }

    public static interface TestInterface {

    }
}
