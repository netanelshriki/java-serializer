package com.serializer.exception;

/**
 * Exception thrown when serialization fails.
 * <p>
 * This exception encapsulates errors that occur during the serialization process,
 * providing detailed information about what went wrong.
 * </p>
 * 
 * @author java-serializer
 */
public class SerializationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new serialization exception with the specified detail message.
     *
     * @param message The detail message
     */
    public SerializationException(String message) {
        super(message);
    }

    /**
     * Constructs a new serialization exception with the specified detail message and cause.
     *
     * @param message The detail message
     * @param cause The cause of the exception
     */
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new serialization exception with the specified cause.
     *
     * @param cause The cause of the exception
     */
    public SerializationException(Throwable cause) {
        super(cause);
    }
}