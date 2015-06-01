package me.ellios.jedis.loadbalancer;

import com.google.common.annotations.GwtCompatible;

/**
 * Determines an output value based on two input values.
 * Specified for load balancer.
 * <p/>
 * <p>See the Guava User Guide article on <a href=
 * "http://code.google.com/p/guava-libraries/wiki/FunctionalExplained">the use of {@code
 * Function}</a>.
 *
 * @author Kevin Bourrillion
 * @since 2.0 (imported from Google Collections Library)
 */
@GwtCompatible
public interface Function<F, T> {
    /**
     * Returns the result of applying this function to {@code input}. This method is <i>generally
     * expected</i>, but not absolutely required, to have the following properties:
     *
     * @throws NullPointerException if {@code input} is null and this function does not accept null
     *                              arguments
     */
    T apply(int index, F input);
}