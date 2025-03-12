package com.serializer.impl;

import java.util.HashMap;
import java.util.Map;

import com.serializer.api.SerializationContext;
import com.serializer.api.SerializerFactory;
import com.serializer.api.TypeAdapter;

/**
 * Default implementation of the {@link SerializationContext} interface.
 * <p>
 * This class provides a concrete implementation of the serialization context,
 * maintaining configuration settings and type adapters for serialization and
 * deserialization operations.
 * </p>
 * 
 * @author java-serializer
 */
public class DefaultSerializationContext implements SerializationContext {
    
    private final SerializerFactory serializerFactory;
    private final Map<Class<?>, TypeAdapter<?, ?>> typeAdapters;
    private final boolean serializeNulls;
    private final boolean useFieldNames;
    private final String dateFormat;
    private final String indentation;
    
    /**
     * Constructs a new serialization context with the specified parameters.
     *
     * @param serializerFactory The serializer factory to use
     * @param serializeNulls Whether null fields should be included in serialization
     * @param useFieldNames Whether field names should be used as-is
     * @param dateFormat The date format pattern to use for serializing date and time objects
     * @param indentation The indentation string to use for pretty printing, or null to disable pretty printing
     */
    public DefaultSerializationContext(SerializerFactory serializerFactory, boolean serializeNulls,
            boolean useFieldNames, String dateFormat, String indentation) {
        this.serializerFactory = serializerFactory;
        this.typeAdapters = new HashMap<>();
        this.serializeNulls = serializeNulls;
        this.useFieldNames = useFieldNames;
        this.dateFormat = dateFormat;
        this.indentation = indentation;
    }
    
    /**
     * Creates a new builder for constructing serialization contexts.
     *
     * @param serializerFactory The serializer factory to use
     * @return A new builder instance
     */
    public static Builder builder(SerializerFactory serializerFactory) {
        return new Builder(serializerFactory);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T, R> TypeAdapter<T, R> getAdapter(Class<T> type) {
        return (TypeAdapter<T, R>) typeAdapters.get(type);
    }
    
    @Override
    public <T, R> SerializationContext registerAdapter(Class<T> type, TypeAdapter<T, R> adapter) {
        typeAdapters.put(type, adapter);
        return this;
    }
    
    @Override
    public SerializerFactory getSerializerFactory() {
        return serializerFactory;
    }
    
    @Override
    public boolean isSerializeNulls() {
        return serializeNulls;
    }
    
    @Override
    public boolean isUseFieldNames() {
        return useFieldNames;
    }
    
    @Override
    public String getDateFormat() {
        return dateFormat;
    }
    
    @Override
    public String getIndentation() {
        return indentation;
    }
    
    /**
     * Builder class for constructing {@link DefaultSerializationContext} instances.
     */
    public static class Builder {
        private final SerializerFactory serializerFactory;
        private final Map<Class<?>, TypeAdapter<?, ?>> typeAdapters = new HashMap<>();
        private boolean serializeNulls = false;
        private boolean useFieldNames = false;
        private String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        private String indentation = null;
        
        private Builder(SerializerFactory serializerFactory) {
            this.serializerFactory = serializerFactory;
        }
        
        /**
         * Sets whether fields with null values should be included in serialization.
         *
         * @param serializeNulls true to include null fields, false to exclude them
         * @return This builder instance for method chaining
         */
        public Builder serializeNulls(boolean serializeNulls) {
            this.serializeNulls = serializeNulls;
            return this;
        }
        
        /**
         * Sets whether fields should be serialized using their declared names.
         *
         * @param useFieldNames true to use field names as-is, false to apply naming strategies
         * @return This builder instance for method chaining
         */
        public Builder useFieldNames(boolean useFieldNames) {
            this.useFieldNames = useFieldNames;
            return this;
        }
        
        /**
         * Sets the date format pattern to use for serializing date and time objects.
         *
         * @param dateFormat A string representing the date format pattern
         * @return This builder instance for method chaining
         */
        public Builder dateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }
        
        /**
         * Enables pretty printing with the specified indentation.
         *
         * @param indentation The indentation string to use
         * @return This builder instance for method chaining
         */
        public Builder prettyPrinting(String indentation) {
            this.indentation = indentation;
            return this;
        }
        
        /**
         * Disables pretty printing.
         *
         * @return This builder instance for method chaining
         */
        public Builder disablePrettyPrinting() {
            this.indentation = null;
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
        public <T, R> Builder registerTypeAdapter(Class<T> type, TypeAdapter<T, R> adapter) {
            typeAdapters.put(type, adapter);
            return this;
        }
        
        /**
         * Builds a new serialization context with the current configuration.
         *
         * @return A new serialization context
         */
        public DefaultSerializationContext build() {
            DefaultSerializationContext context = new DefaultSerializationContext(
                    serializerFactory, serializeNulls, useFieldNames, dateFormat, indentation);
            
            // Register type adapters
            for (Map.Entry<Class<?>, TypeAdapter<?, ?>> entry : typeAdapters.entrySet()) {
                // Fix: Use raw types with suppressed warnings to avoid type inference issues
                @SuppressWarnings({"unchecked", "rawtypes"})
                Class rawType = entry.getKey();
                @SuppressWarnings({"unchecked", "rawtypes"})
                TypeAdapter rawAdapter = entry.getValue();
                context.registerAdapter(rawType, rawAdapter);
            }
            
            return context;
        }
    }
}