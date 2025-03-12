package com.serializer.builder;

import com.serializer.api.SerializationContext;
import com.serializer.api.Serializer;
import com.serializer.api.TypeAdapter;
import com.serializer.json.JsonSerializer;
import com.serializer.json.JsonSerializerFactory;

/**
 * Builder class for creating JSON serializers with a fluent API.
 * <p>
 * This class provides a convenient way to configure and create JSON serializers
 * with various options and type adapters.
 * </p>
 * 
 * @param <T> The type of object to serialize
 * @author java-serializer
 */
public class JsonSerializerBuilder<T> {
    
    private final Class<T> type;
    private final JsonSerializerFactory.Builder factoryBuilder;
    
    /**
     * Constructs a new builder for the specified type.
     *
     * @param type The class object representing the type to serialize
     */
    public JsonSerializerBuilder(Class<T> type) {
        this.type = type;
        this.factoryBuilder = JsonSerializerFactory.builder();
    }
    
    /**
     * Sets whether fields with null values should be included in serialization.
     *
     * @param serializeNulls true to include null fields, false to exclude them
     * @return This builder instance for method chaining
     */
    public JsonSerializerBuilder<T> serializeNulls(boolean serializeNulls) {
        factoryBuilder.serializeNulls(serializeNulls);
        return this;
    }
    
    /**
     * Sets whether fields should be serialized using their declared names.
     *
     * @param useFieldNames true to use field names as-is, false to apply naming strategies
     * @return This builder instance for method chaining
     */
    public JsonSerializerBuilder<T> useFieldNames(boolean useFieldNames) {
        factoryBuilder.useFieldNames(useFieldNames);
        return this;
    }
    
    /**
     * Sets the date format pattern to use for serializing date and time objects.
     *
     * @param dateFormat A string representing the date format pattern
     * @return This builder instance for method chaining
     */
    public JsonSerializerBuilder<T> dateFormat(String dateFormat) {
        factoryBuilder.dateFormat(dateFormat);
        return this;
    }
    
    /**
     * Enables pretty printing with the specified indentation.
     *
     * @param indentation The indentation string to use
     * @return This builder instance for method chaining
     */
    public JsonSerializerBuilder<T> prettyPrinting(String indentation) {
        factoryBuilder.prettyPrinting(indentation);
        return this;
    }
    
    /**
     * Disables pretty printing.
     *
     * @return This builder instance for method chaining
     */
    public JsonSerializerBuilder<T> disablePrettyPrinting() {
        factoryBuilder.disablePrettyPrinting();
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
    public <A, B> JsonSerializerBuilder<T> registerTypeAdapter(Class<A> type, TypeAdapter<A, B> adapter) {
        factoryBuilder.registerTypeAdapter(type, adapter);
        return this;
    }
    
    /**
     * Builds a new JSON serializer with the current configuration.
     *
     * @return A new JSON serializer
     */
    public Serializer<T> build() {
        return factoryBuilder.build().getSerializer(type);
    }
}