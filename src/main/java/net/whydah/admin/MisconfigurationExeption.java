package net.whydah.admin;

/**
 * Thrown when authentication fails.
 */
public class MisconfigurationExeption extends RuntimeException{
    public MisconfigurationExeption() {
    }

    public MisconfigurationExeption(String message) {
        super(message);
    }

    public MisconfigurationExeption(String message, Throwable cause) {
        super(message, cause);
    }

    public MisconfigurationExeption(Throwable cause) {
        super(cause);
    }
}
