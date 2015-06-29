package me.ellios.memcached.exception;

import me.ellios.hedwig.common.exceptions.HedwigException;

/**
 * User: ellios
 * Time: 15-6-29 : 上午12:22
 */
public class HedwigMemcachedException extends HedwigException{

    public HedwigMemcachedException(String message) {
        super(message);
    }

    public HedwigMemcachedException(Throwable cause) {
        super(cause);
    }
}
