package com.serializer.impl;

import java.util.HashMap;
import java.util.Map;

import com.serializer.api.Deserializer;
import com.serializer.api.Serializer;
import com.serializer.api.SerializerFactory;
import com.serializer.exception.DeserializationException;
import com.serializer.exception.SerializationException;

/**
 * Default implementation of the {@link SerializerFactory} interface.
 * <p>
 * This class provides a concrete implementation of the serializer factory,
 * managing the creation and caching of serializer and deserializer instances
 * for different object types.
 * </p>
 * 
 * @author java-serializer
 */
public class DefaultSerializerFactory implements SerializerFactory {
    
    private final Map<Class<?>, Serializer<?>> serializers;
    private final Map<Class<?>, Deserializer<?>> deserializers;
    
    /**
     * Constructs a new serializer factory with no pre-registered serializers or deserializers.
     */
    public DefaultSerializerFactory() {
        this.serializers = new HashMap<>();
        this.deserializers = new HashMap<>();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> Serializer<T> getSerializer(Class<T> type) {
        Serializer<T> serializer = (Serializer<T>) serializers.get(type);
        if (serializer == null) {
            serializer = createSerializer(type);
            serializers.put(type, serializer);
        }
        return serializer;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> Deserializer<T> getDeserializer(Class<T> type) {
        Deserializer<T> deserializer = (Deserializer<T>) deserializers.get(type);
        if (deserializer == null) {
            deserializer = createDeserializer(type);
            deserializers.put(type, deserializer);
        }
        return deserializer;
    }
    
    @Override
    public <T> SerializerFactory registerSerializer(Class<T> type, Serializer<T> serializer) {
        serializers.put(type, serializer);
        return this;
    }
    
    @Override
    public <T> SerializerFactory registerDeserializer(Class<T> type, Deserializer<T> deserializer) {
        deserializers.put(type, deserializer);
        return this;
    }
    
    /**
     * Creates a new serializer for the specified type.
     * <p>
     * This method is called when a serializer for a type is requested but not found in the cache.
     * </p>
     *
     * @param <T> The type for which to create a serializer
     * @param type The class object representing the type
     * @return A new serializer for the specified type
     * @throws SerializationException if a serializer cannot be created
     */
    protected <T> Serializer<T> createSerializer(Class<T> type) {
        throw new SerializationException("No serializer registered for type: " + type.getName());
    }
    
    /**
     * Creates a new deserializer for the specified type.
     * <p>
     * This method is called when a deserializer for a type is requested but not found in the cache.
     * </p>
     *
     * @param <T> The type for which to create a deserializer
     * @param type The class object representing the type
     * @return A new deserializer for the specified type
     * @throws DeserializationException if a deserializer cannot be created
     */
    protected <T> Deserializer<T> createDeserializer(Class<T> type) {
        throw new DeserializationException("No deserializer registered for type: " + type.getName());
    }
}