package de.itemis.mosig.fluffy.tests.java.sneaky;

import de.itemis.mosig.fluffy.tests.java.InstantiationNotPermittedException;

/**
 * <p>
 * Static helper class around the sneaky throws paradigm.
 * </p>
 *
 * @see <a href=
 *      "http://www.philandstuff.com/2012/04/28/sneakily-throwing-checked-exceptions.html">http://www.philandstuff.com/2012/04/28/sneakily-throwing-checked-exceptions.html</a>
 */
public final class Sneaky {

    private Sneaky() {
        throw new InstantiationNotPermittedException();
    }

    /**
     * <p>
     * Rethrow the provided throwable without declaring it to be thrown.
     * </p>
     * <p>
     * Example:
     *
     * <pre>
     *  *
     *  * This method throws an IOException even though it has not been declared.
     *  *
     *  *
     * public void operation() {
     *   Sneaky.throwThat(new IOException());
     * }
     * </pre>
     * </p>
     *
     * @param t - The {@link Throwable} to rethrow.
     * @throws T - Will be inferred to an instance of {@link RuntimeException} by the compiler.
     * @see <a href=
     *      "http://www.philandstuff.com/2012/04/28/sneakily-throwing-checked-exceptions.html">http://www.philandstuff.com/2012/04/28/sneakily-throwing-checked-exceptions.html</a>
     */
    /*
     * The method uses the so called sneaky throws paradigm. This requires an "unchecked" cast.
     * However, the cast can be considered "safe", because at compile time it may be unsafe, but at
     * runtime the cast disappears. Because the type compatibility check for the throws statement is
     * done at compile time, removing the cast at runtime (due to type erasure) does no harm. It is
     * allowed to throw any type of Throwable.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> void throwThat(Throwable t) throws T {
        throw (T) t;
    }
}
