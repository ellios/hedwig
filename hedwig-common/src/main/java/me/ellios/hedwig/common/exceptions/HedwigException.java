package me.ellios.hedwig.common.exceptions;

/**
 * Author: ellios
 * Date: 12-10-31 Time: 上午10:13
 */
public class HedwigException extends RuntimeException{

    private static final long serialVersionUID = 7815426752583648734L;

    public HedwigException() {
        super();
    }

    public HedwigException(String message, Throwable cause) {
        super(message, cause);
    }

    public HedwigException(String message) {
        super(message);
    }

    public HedwigException(Throwable cause) {
        super(cause);
    }
}
