package me.ellios.hedwig.http.provider;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/23/13 5:39 PM
 */
public interface TypeChecker {
    boolean isSupported(Class<?> type);
}
