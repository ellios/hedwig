package me.ellios.hedwig.http.provider;

import javax.ws.rs.core.MediaType;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/15/13 7:37 PM
 */
public interface EntityProvider {
    TypeChecker getTypeChecker();

    boolean isCompatible(MediaType mediaType);
}
