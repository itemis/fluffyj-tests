package de.itemis.mosig.fluffy.tests.java;

/**
 * Collection of utility exceptions that may be used in unit tests.
 */
public final class ExpectedExceptions {
    public static final String EXPECTED_MESSAGE = "Expected exception. Please ignore.";
    public static final RuntimeException EXPECTED_UNCHECKED_EXCEPTION = new RuntimeException(EXPECTED_MESSAGE);
    public static final Exception EXPECTED_CHECKED_EXCEPTION = new Exception(EXPECTED_MESSAGE);

    private ExpectedExceptions() {
        throw new InstantiationNotPermittedException();
    }
}
