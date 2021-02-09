package de.itemis.mosig.fluffy.tests.java.exceptions;

/**
 * Collection of utility exceptions that may be used in unit tests.
 */
public final class ExpectedExceptions {
    /**
     * Every exception listed here is going to have this {@link String} set as its message.
     */
    public static final String EXPECTED_MESSAGE = "Expected exception. Please ignore.";
    /**
     * An unchecked exception to expect in test code.
     */
    public static final RuntimeException EXPECTED_UNCHECKED_EXCEPTION = new RuntimeException(EXPECTED_MESSAGE);
    /**
     * A checked exception to expect in test code.
     */
    public static final Exception EXPECTED_CHECKED_EXCEPTION = new Exception(EXPECTED_MESSAGE);

    private ExpectedExceptions() {
        throw new InstantiationNotPermittedException();
    }
}
