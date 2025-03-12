package com.serializer.exception;

/**
 * Exception thrown when a type adapter fails to adapt a type.
 * <p>
 * This exception encapsulates errors that occur during type adaptation,
 * providing detailed information about what went wrong.
 * </p>
 * 
 * @author java-serializer
 */
public class TypeAdapterException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final Class<?> type;

    /**
     * Constructs a new type adapter exception with the specified detail message and type.
     *
     * @param message The detail message
     * @param type The class of the type that couldn't be adapted
     */
    public TypeAdapterException(String message, Class<?> type) {
        super(message);
        this.type = type;
    }

    /**
     * Constructs a new type adapter exception with the specified detail message, cause, and type.
     *
     * @param message The detail message
     * @param cause The cause of the exception
     * @param type The class of the type that couldn't be adapted
     */
    public TypeAdapterException(String message, Throwable cause, Class<?> type) {
        super(message, cause);
        this.type = type;
    }

    /**
     * Gets the class of the type that couldn't be adapted.
     *
     * @return The class of the type
     */
    public Class<?> getType() {
        return type;
    }
}