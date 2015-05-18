package me.ellios.hedwig.http.provider;

import java.util.Collection;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/15/13 7:08 PM
 */
public interface CollectionWriter<Out> {
    void write(Collection collect, Class<?> elementClass, Out op) throws Exception;
}
