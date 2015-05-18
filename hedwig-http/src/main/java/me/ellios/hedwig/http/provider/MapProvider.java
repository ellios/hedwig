package me.ellios.hedwig.http.provider;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/05/13 6:46 PM
 */
public interface MapProvider {
    Class<?>[] getMapImplementations();

    boolean verifyMapSubclass(Class<?> type);
}
