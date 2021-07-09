package de.itemis.mosig.fluffy.tests.java.exceptions;

import static java.util.Objects.requireNonNull;

public final class ThrowablePrettyfier {

    private ThrowablePrettyfier() {
        throw new InstantiationNotPermittedException();
    }

    /**
     * Construct a nice message out of the throwable's type and its message (if any). May be used
     * for logging purposes.
     * 
     * @param t The {@link Throwable} to construct the message for.
     * @return A nice message.
     */
    public static String pretty(Throwable t) {
        requireNonNull(t, "t");

        var msg = t.getMessage() == null ? "No further information" : t.getMessage();
        return t.getClass().getSimpleName() + ": " + msg;
    }
}
