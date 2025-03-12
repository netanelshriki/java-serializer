package com.serializer.api;

/**
 * Interface for adapting types during serialization and deserialization.
 * <p>
 * Type adapters provide custom logic for serializing and deserializing specific types
 * that may require special handling. They are used by serializers and deserializers
 * to handle complex types or types with custom serialization requirements.
 * </p>
 * 
 * @param <T> The Java type to adapt
 * @param <R> The serialized representation type
 * @author java-serializer
 */
public interface TypeAdapter<T, R> {
    
    /**
     * Converts a value of type T to its serialized representation of type R.
     *
     * @param value The value to serialize
     * @return The serialized representation
     * @throws com.serializer.exception.SerializationException if serialization fails
     */
    R serialize(T value);
    
    /**
     * Converts a serialized representation of type R back to its original type T.
     *
     * @param value The serialized representation
     * @return The deserialized value
     * @throws com.serializer.exception.DeserializationException if deserialization fails
     */
    T deserialize(R value);
    
    /**
     * Gets the Java type this adapter handles.
     *
     * @return The class object representing the type T
     */
    Class<T> getType();
}