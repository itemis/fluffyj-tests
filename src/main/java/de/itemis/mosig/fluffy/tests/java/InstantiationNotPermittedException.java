package de.itemis.mosig.fluffy.tests.java;

/**
 * <p>
 * Use this exception to mark class contructors as being off limits.
 * </p>
 * <p>
 * Example:
 *
 * <pre>
 * public final class Clazz() {
 *     private Clazz() {
 *         throw new InstantionNotPermittedException();
 *     }
 * }
 * </pre>
 * </p>
 */
public final class InstantiationNotPermittedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InstantiationNotPermittedException() {
        super("Instantiation of class not permitted.");
    }
}
