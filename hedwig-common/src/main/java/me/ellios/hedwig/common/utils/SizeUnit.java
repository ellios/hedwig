package me.ellios.hedwig.common.utils;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/18/13 4:46 PM
 */

public enum SizeUnit {
    KILOBYTE,
    MEGABYTE
    ;

    public long toKilobytes() {
        throw new AbstractMethodError();
    }
}

