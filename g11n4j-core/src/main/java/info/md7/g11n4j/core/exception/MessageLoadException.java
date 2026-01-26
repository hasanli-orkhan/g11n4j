package info.md7.g11n4j.core.exception;

/**
 * Exception thrown when message files cannot be loaded from the resource path.
 */
public class MessageLoadException extends RuntimeException {

    public MessageLoadException(String filename, Throwable cause) {
        super("Failed to load message file: " + filename, cause);
    }

    public MessageLoadException(String message) {
        super(message);
    }
}
