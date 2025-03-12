package com.serializer.api;

import java.io.OutputStream;
import java.io.Writer;

/**
 * Core interface for serializing Java objects into different formats.
 * <p>
 * This interface defines the contract for serialization operations that convert
 * Java objects into a serialized format. Implementations may support various
 * output formats such as JSON, XML, or binary.
 * </p>
 * 
 * @param <T> The type of object to be serialized
 * @author java-serializer
 */
public interface Serializer<T> {
    
    /**
     * Serializes an object to a string representation.
     *
     * @param object The object to serialize
     * @return A string containing the serialized representation
     * @throws com.serializer.exception.SerializationException if serialization fails
     */
    String serialize(T object);
    
    /**
     * Serializes an object and writes the result to the provided writer.
     *
     * @param object The object to serialize
     * @param writer The writer to which the serialized data will be written
     * @throws com.serializer.exception.SerializationException if serialization fails
     */
    void serialize(T object, Writer writer);
    
    /**
     * Serializes an object and writes the result to the provided output stream.
     * This method is particularly useful for binary serialization formats.
     *
     * @param object The object to serialize
     * @param outputStream The output stream to which the serialized data will be written
     * @throws com.serializer.exception.SerializationException if serialization fails
     */
    void serialize(T object, OutputStream outputStream);
    
    /**
     * Returns the content type of the serialized format.
     * For example, "application/json" for JSON serialization.
     *
     * @return A string representing the content type of the serialized format
     */
    String getContentType();
}