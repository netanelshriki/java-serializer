package com.serializer.api;

import java.io.InputStream;
import java.io.Reader;

/**
 * Core interface for deserializing data from various formats into Java objects.
 * <p>
 * This interface defines the contract for deserialization operations that convert
 * serialized data back into Java objects. Implementations may support various
 * input formats such as JSON, XML, or binary.
 * </p>
 * 
 * @param <T> The type of object to deserialize into
 * @author java-serializer
 */
public interface Deserializer<T> {
    
    /**
     * Deserializes data from a string into an object of type T.
     *
     * @param serializedData The string containing serialized data
     * @return An object of type T represented by the serialized data
     * @throws com.serializer.exception.DeserializationException if deserialization fails
     */
    T deserialize(String serializedData);
    
    /**
     * Deserializes data from a reader into an object of type T.
     *
     * @param reader The reader containing serialized data
     * @return An object of type T represented by the serialized data
     * @throws com.serializer.exception.DeserializationException if deserialization fails
     */
    T deserialize(Reader reader);
    
    /**
     * Deserializes data from an input stream into an object of type T.
     * This method is particularly useful for binary deserialization formats.
     *
     * @param inputStream The input stream containing serialized data
     * @return An object of type T represented by the serialized data
     * @throws com.serializer.exception.DeserializationException if deserialization fails
     */
    T deserialize(InputStream inputStream);
    
    /**
     * Gets the class of objects this deserializer produces.
     *
     * @return The class object representing the type T
     */
    Class<T> getTargetClass();
}