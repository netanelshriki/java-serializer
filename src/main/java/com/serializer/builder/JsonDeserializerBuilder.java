package com.serializer.builder;

import com.serializer.api.Deserializer;
import com.serializer.api.TypeAdapter;
import com.serializer.json.JsonSerializerFactory;

/**
 * Builder class for creating JSON deserializers with a fluent API.
 * <p>
 * This class provides a convenient way to configure and create JSON deserializers
 * with various options and type adapters.
 * </p>
 * 
 * @param <T> The type of object to deserialize
 * @author java-serializer
 */
public class JsonDeserializerBuilder<T> {
    
    private final Class<T> type;
    private final JsonSerializerFactory.Builder factoryBuilder;
    
    /**
     * Constructs a new builder for the specified type.
     *
     * @param type The class object representing the type to deserialize
     */
    public JsonDeserializerBuilder(Class<T> type) {
        this.type = type;
        this.factoryBuilder = JsonSerializerFactory.builder();
    }
    
    /**
     * Sets whether fields with null values should be included in serialization.
     *
     * @param serializeNulls true to include null fields, false to exclude them
     * @return This builder instance for method chaining
     */
    public JsonDeserializerBuilder<T> serializeNulls(boolean serializeNulls) {
        factoryBuilder.serializeNulls(serializeNulls);
        return this;
    }
    
    /**
     * Sets whether fields should be serialized using their declared names.
     *
     * @param useFieldNames true to use field names as-is, false to apply naming strategies
     * @return This builder instance for method chaining
     */
    public JsonDeserializerBuilder<T> useFieldNames(boolean useFieldNames) {
        factoryBuilder.useFieldNames(useFieldNames);
        return this;
    }
    
    /**
     * Sets the date format pattern to use for serializing date and time objects.
     *
     * @param dateFormat A string representing the date format pattern
     * @return This builder instance for method chaining
     */
    public JsonDeserializerBuilder<T> dateFormat(String dateFormat) {
        factoryBuilder.dateFormat(dateFormat);
        return this;
    }
    
    /**
     * Registers a type adapter for a specific type.
     *
     * @param <A> The type for which to register the adapter
     * @param <B> The serialized representation type
     * @param type The class object representing the type
     * @param adapter The type adapter to register
     * @return This builder instance for method chaining
     */
    public <A, B> JsonDeserializerBuilder<T> registerTypeAdapter(Class<A> type, TypeAdapter<A, B> adapter) {
        factoryBuilder.registerTypeAdapter(type, adapter);
        return this;
    }
    
    /**
     * Builds a new JSON deserializer with the current configuration.
     *
     * @return A new JSON deserializer
     */
    public Deserializer<T> build() {
        return factoryBuilder.build().getDeserializer(type);
    }
}