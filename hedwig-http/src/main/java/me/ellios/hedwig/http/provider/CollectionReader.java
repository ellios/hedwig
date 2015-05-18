package me.ellios.hedwig.http.provider;

import java.util.Collection;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/17/13 7:06 PM
 */
public interface CollectionReader<In> extends CollectionProvider {
    void read(Collection collect, Class<?> elementType, In ip) throws Exception;

    boolean verifyCollectionSubclass(Class<?> type);

}
