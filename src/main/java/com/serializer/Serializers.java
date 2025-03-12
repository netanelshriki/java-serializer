package com.serializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import com.serializer.adapter.DateTypeAdapter;
import com.serializer.adapter.EnumTypeAdapter;
import com.serializer.adapter.LocalDateTimeTypeAdapter;
import com.serializer.adapter.UUIDTypeAdapter;
import com.serializer.adapter.URLTypeAdapter;
import com.serializer.api.Deserializer;
import com.serializer.api.Serializer;
import com.serializer.api.SerializerFactory;
import com.serializer.api.TypeAdapter;
import com.serializer.json.JsonSerializerFactory;

/**
 * Main facade class for the serialization library.
 * <p>
 * This class provides a simple entry point for users of the library, with factory methods
 * for creating serializers and deserializers for different formats. It also includes
 * convenience methods for common serialization and deserialization operations.
 * </p>
 * 
 * @author java-serializer
 */
public final class Serializers {
    
    private Serializers() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Creates a new JSON serializer for the specified type.
     *
     * @param <T> The type of object to serialize
     * @param type The class object representing the type to serialize
     * @return A new JSON serializer
     */
    public static <T> Serializer<T> jsonSerializer(Class<T> type) {
        return createDefaultJsonFactory().getSerializer(type);
    }
    
    /**
     * Creates a new JSON deserializer for the specified type.
     *
     * @param <T> The type of object to deserialize
     * @param type The class object representing the type to deserialize
     * @return A new JSON deserializer
     */
    public static <T> Deserializer<T> jsonDeserializer(Class<T> type) {
        return createDefaultJsonFactory().getDeserializer(type);
    }
    
    /**
     * Creates a builder for a JSON serializer factory.
     *
     * @return A new builder instance
     */
    public static JsonSerializerFactory.Builder jsonBuilder() {
        return JsonSerializerFactory.builder();
    }
    
    /**
     * Serializes an object to JSON.
     *
     * @param <T> The type of object to serialize
     * @param object The object to serialize
     * @return A JSON string
     */
    public static <T> String toJson(T object) {
        if (object == null) {
            return "null";
        }
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) object.getClass();
        return jsonSerializer(type).serialize(object);
    }
    
    /**
     * Serializes an object to JSON and writes it to the specified writer.
     *
     * @param <T> The type of object to serialize
     * @param object The object to serialize
     * @param writer The writer to write the JSON to
     */
    public static <T> void toJson(T object, Writer writer) {
        if (object == null) {
            try {
                writer.write("null");
                return;
            } catch (java.io.IOException e) {
                throw new com.serializer.exception.SerializationException("Failed to write null", e);
            }
        }
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) object.getClass();
        jsonSerializer(type).serialize(object, writer);
    }
    
    /**
     * Serializes an object to JSON and writes it to the specified output stream.
     *
     * @param <T> The type of object to serialize
     * @param object The object to serialize
     * @param outputStream The output stream to write the JSON to
     */
    public static <T> void toJson(T object, OutputStream outputStream) {
        if (object == null) {
            try {
                outputStream.write("null".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                return;
            } catch (java.io.IOException e) {
                throw new com.serializer.exception.SerializationException("Failed to write null", e);
            }
        }
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) object.getClass();
        jsonSerializer(type).serialize(object, outputStream);
    }
    
    /**
     * Deserializes a JSON string to an object of the specified type.
     *
     * @param <T> The type of object to deserialize
     * @param json The JSON string to deserialize
     * @param type The class object representing the type to deserialize
     * @return The deserialized object
     */
    public static <T> T fromJson(String json, Class<T> type) {
        return jsonDeserializer(type).deserialize(json);
    }
    
    /**
     * Deserializes JSON from a reader to an object of the specified type.
     *
     * @param <T> The type of object to deserialize
     * @param reader The reader providing the JSON to deserialize
     * @param type The class object representing the type to deserialize
     * @return The deserialized object
     */
    public static <T> T fromJson(Reader reader, Class<T> type) {
        return jsonDeserializer(type).deserialize(reader);
    }
    
    /**
     * Deserializes JSON from an input stream to an object of the specified type.
     *
     * @param <T> The type of object to deserialize
     * @param inputStream The input stream providing the JSON to deserialize
     * @param type The class object representing the type to deserialize
     * @return The deserialized object
     */
    public static <T> T fromJson(InputStream inputStream, Class<T> type) {
        return jsonDeserializer(type).deserialize(inputStream);
    }
    
    /**
     * Creates a default JSON serializer factory with pre-registered type adapters.
     *
     * @return A new JSON serializer factory
     */
    private static SerializerFactory createDefaultJsonFactory() {
        return jsonBuilder()
                .dateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .prettyPrinting("  ")
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(java.net.URL.class, new URLTypeAdapter())
                .build();
    }
}