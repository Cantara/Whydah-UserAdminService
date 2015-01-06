package net.whydah.admin;

/**
 * Thrown when Connection to external service fails.
 */
public class ConnectionFailedException extends RuntimeException{
    public ConnectionFailedException() {
    }

    public ConnectionFailedException(String message) {
        super(message);
    }

    public ConnectionFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionFailedException(Throwable cause) {
        super(cause);
    }
}
