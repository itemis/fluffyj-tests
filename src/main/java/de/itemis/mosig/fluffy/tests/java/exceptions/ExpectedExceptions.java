package de.itemis.mosig.fluffy.tests.java.exceptions;

/**
 * Collection of utility exceptions that may be used in unit tests.
 */
public final class ExpectedExceptions {
    /**
     * Every {@link Exception} listed here is going to have this {@link String} set as its message.
     */
    public static final String EXPECTED_MESSAGE = "Expected exception. Please ignore.";

    /**
     * The expected {@link Throwable} has this message set.
     */
    public static final String EXPECTED_THROWABLE_MESSAGE = "Expected throwable. Please ignore.";

    /**
     * The expected {@link Error} has this message set.
     */
    public static final String EXPECTED_ERROR_MESSAGE = "Expected error. Please ignore.";

    /**
     * An unchecked exception to be expect in test code.
     */
    public static final RuntimeException EXPECTED_UNCHECKED_EXCEPTION = new RuntimeException(EXPECTED_MESSAGE);

    /**
     * A checked exception to be expect in test code.
     */
    public static final Exception EXPECTED_CHECKED_EXCEPTION = new Exception(EXPECTED_MESSAGE);

    /**
     * A {@link Throwable} to be expected in test code.
     */
    public static final Throwable EXPECTED_THROWABLE = new Throwable(EXPECTED_THROWABLE_MESSAGE);

    /**
     * An {@link Error} to be expected in test code.
     */
    public static final Error EXPECTED_ERROR = new Error(EXPECTED_ERROR_MESSAGE);

    private ExpectedExceptions() {
        throw new InstantiationNotPermittedException();
    }
}
