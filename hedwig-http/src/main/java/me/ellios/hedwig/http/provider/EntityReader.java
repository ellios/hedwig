package me.ellios.hedwig.http.provider;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/23/13 5:11 PM
 */
public interface EntityReader<In, Out> {
    Out read(In input) throws Exception;
}
