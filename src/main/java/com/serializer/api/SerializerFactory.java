package com.serializer.api;

/**
 * Factory interface for creating serializers and deserializers.
 * <p>
 * This interface defines the contract for creating serializer and deserializer instances
 * for different object types. Implementations manage the creation and caching of
 * appropriate serializer/deserializer instances.
 * </p>
 * 
 * @author java-serializer
 */
public interface SerializerFactory {
    
    /**
     * Creates or retrieves a serializer for the specified type.
     *
     * @param <T> The type of object to serialize
     * @param type The class object representing the type to serialize
     * @return A serializer capable of serializing objects of the specified type
     * @throws com.serializer.exception.SerializationException if a serializer cannot be created
     */
    <T> Serializer<T> getSerializer(Class<T> type);
    
    /**
     * Creates or retrieves a deserializer for the specified type.
     *
     * @param <T> The type of object to deserialize
     * @param type The class object representing the type to deserialize
     * @return A deserializer capable of deserializing objects of the specified type
     * @throws com.serializer.exception.DeserializationException if a deserializer cannot be created
     */
    <T> Deserializer<T> getDeserializer(Class<T> type);
    
    /**
     * Registers a custom serializer for a specific type.
     *
     * @param <T> The type for which to register the serializer
     * @param type The class object representing the type
     * @param serializer The serializer to register
     * @return This factory instance for method chaining
     */
    <T> SerializerFactory registerSerializer(Class<T> type, Serializer<T> serializer);
    
    /**
     * Registers a custom deserializer for a specific type.
     *
     * @param <T> The type for which to register the deserializer
     * @param type The class object representing the type
     * @param deserializer The deserializer to register
     * @return This factory instance for method chaining
     */
    <T> SerializerFactory registerDeserializer(Class<T> type, Deserializer<T> deserializer);
}