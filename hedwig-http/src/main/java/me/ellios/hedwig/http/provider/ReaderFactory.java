package me.ellios.hedwig.http.provider;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/15/13 5:30 PM
 */
public interface ReaderFactory<Input, Out> {
    EntityReader<Input, Out> getEntityReader(final Class<?> klass);
}
