package com.serializer.exception;

/**
 * Exception thrown when deserialization fails.
 * <p>
 * This exception encapsulates errors that occur during the deserialization process,
 * providing detailed information about what went wrong.
 * </p>
 * 
 * @author java-serializer
 */
public class DeserializationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new deserialization exception with the specified detail message.
     *
     * @param message The detail message
     */
    public DeserializationException(String message) {
        super(message);
    }

    /**
     * Constructs a new deserialization exception with the specified detail message and cause.
     *
     * @param message The detail message
     * @param cause The cause of the exception
     */
    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new deserialization exception with the specified cause.
     *
     * @param cause The cause of the exception
     */
    public DeserializationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new deserialization exception with the specified detail message, cause,
     * position in the input, and the snippet of input around the error.
     *
     * @param message The detail message
     * @param cause The cause of the exception
     * @param position The position in the input where the error occurred
     * @param inputSnippet A snippet of the input around the error
     */
    public DeserializationException(String message, Throwable cause, int position, String inputSnippet) {
        super(buildDetailedMessage(message, position, inputSnippet), cause);
    }
    
    /**
     * Builds a detailed error message including position information.
     *
     * @param message The base error message
     * @param position The position in the input where the error occurred
     * @param inputSnippet A snippet of the input around the error
     * @return A detailed error message
     */
    private static String buildDetailedMessage(String message, int position, String inputSnippet) {
        StringBuilder sb = new StringBuilder(message);
        sb.append(" at position ").append(position);
        if (inputSnippet != null && !inputSnippet.isEmpty()) {
            sb.append(", near: ").append(inputSnippet);
        }
        return sb.toString();
    }
}