package me.ellios.hedwig.http.provider;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/23/13 5:12 PM
 */
public interface EntityWriter<Out> {
    void write(Object obj, Out output) throws Exception;
}
