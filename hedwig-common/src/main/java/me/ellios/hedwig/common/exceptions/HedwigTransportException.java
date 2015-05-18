package me.ellios.hedwig.common.exceptions;



public class HedwigTransportException extends RuntimeException{

    private static final long serialVersionUID = 7815426752583648734L;

    public HedwigTransportException() {
        super();
    }

    public HedwigTransportException(String message, Throwable cause) {
        super(message, cause);
    }

    public HedwigTransportException(String message) {
        super(message);
    }

    public HedwigTransportException(Throwable cause) {
        super(cause);
    }
}
