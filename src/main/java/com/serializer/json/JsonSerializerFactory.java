package com.serializer.json;

import com.serializer.api.Deserializer;
import com.serializer.api.SerializationContext;
import com.serializer.api.Serializer;
import com.serializer.api.SerializerFactory;
import com.serializer.impl.DefaultSerializerFactory;

/**
 * Factory for creating JSON serializers and deserializers.
 * <p>
 * This class extends the {@link DefaultSerializerFactory} and provides
 * implementations of the {@link #createSerializer} and {@link #createDeserializer}
 * methods that return JSON-specific serializers and deserializers.
 * </p>
 * 
 * @author java-serializer
 */
public class JsonSerializerFactory extends DefaultSerializerFactory {
    
    private final SerializationContext context;
    
    /**
     * Constructs a new JSON serializer factory with the specified context.
     *
     * @param context The serialization context to use
     */
    public JsonSerializerFactory(SerializationContext context) {
        this.context = context;
    }
    
    @Override
    protected <T> Serializer<T> createSerializer(Class<T> type) {
        return new JsonSerializer<>(type, context);
    }
    
    @Override
    protected <T> Deserializer<T> createDeserializer(Class<T> type) {
        return new JsonDeserializer<>(type, context);
    }
    
    /**
     * Creates a builder for a JSON serializer factory.
     *
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for creating {@link JsonSerializerFactory} instances with
     * a fluent API.
     */
    public static class Builder {
        private final com.serializer.impl.DefaultConfig config;
        
        /**
         * Constructs a new builder with default settings.
         */
        private Builder() {
            // Create a temporary factory that will be replaced later
            this.config = new com.serializer.impl.DefaultConfig(new DefaultSerializerFactory());
        }
        
        /**
         * Sets whether fields with null values should be included in serialization.
         *
         * @param serializeNulls true to include null fields, false to exclude them
         * @return This builder instance for method chaining
         */
        public Builder serializeNulls(boolean serializeNulls) {
            config.serializeNulls(serializeNulls);
            return this;
        }
        
        /**
         * Sets whether fields should be serialized using their declared names.
         *
         * @param useFieldNames true to use field names as-is, false to apply naming strategies
         * @return This builder instance for method chaining
         */
        public Builder useFieldNames(boolean useFieldNames) {
            config.useFieldNames(useFieldNames);
            return this;
        }
        
        /**
         * Sets the date format pattern to use for serializing date and time objects.
         *
         * @param dateFormat A string representing the date format pattern
         * @return This builder instance for method chaining
         */
        public Builder dateFormat(String dateFormat) {
            config.dateFormat(dateFormat);
            return this;
        }
        
        /**
         * Enables pretty printing with the specified indentation.
         *
         * @param indentation The indentation string to use
         * @return This builder instance for method chaining
         */
        public Builder prettyPrinting(String indentation) {
            config.prettyPrinting(indentation);
            return this;
        }
        
        /**
         * Disables pretty printing.
         *
         * @return This builder instance for method chaining
         */
        public Builder disablePrettyPrinting() {
            config.disablePrettyPrinting();
            return this;
        }
        
        /**
         * Registers a type adapter for a specific type.
         *
         * @param <T> The type for which to register the adapter
         * @param <R> The serialized representation type
         * @param type The class object representing the type
         * @param adapter The type adapter to register
         * @return This builder instance for method chaining
         */
        public <T, R> Builder registerTypeAdapter(Class<T> type, com.serializer.api.TypeAdapter<T, R> adapter) {
            config.registerTypeAdapter(type, adapter);
            return this;
        }
        
        /**
         * Builds a new JSON serializer factory with the current configuration.
         *
         * @return A new JSON serializer factory
         */
        public JsonSerializerFactory build() {
            // Create the serialization context
            SerializationContext context = config.createContext();
            
            // Create the factory with the context
            JsonSerializerFactory factory = new JsonSerializerFactory(context);
            
            return factory;
        }
    }
}