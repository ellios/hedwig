package me.ellios.hedwig.http.provider;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/15/13 5:31 PM
 */
public interface WriterFactory<Out> {
    EntityWriter<Out> getEntityWriter(Class<?> clazz);
}
