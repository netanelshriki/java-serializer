package com.serializer.impl;

import com.serializer.api.Config;
import com.serializer.api.SerializationContext;
import com.serializer.api.SerializerFactory;
import com.serializer.api.TypeAdapter;

/**
 * Default implementation of the {@link Config} interface.
 * <p>
 * This class provides a concrete implementation of the configuration object,
 * with builder-style methods for setting various serialization and deserialization options.
 * </p>
 * 
 * @author java-serializer
 */
public class DefaultConfig implements Config<DefaultConfig> {
    
    private final SerializerFactory serializerFactory;
    private final DefaultSerializationContext.Builder contextBuilder;
    
    /**
     * Constructs a new configuration with the specified serializer factory.
     *
     * @param serializerFactory The serializer factory to use
     */
    public DefaultConfig(SerializerFactory serializerFactory) {
        this.serializerFactory = serializerFactory;
        this.contextBuilder = DefaultSerializationContext.builder(serializerFactory);
    }
    
    @Override
    public DefaultConfig serializeNulls(boolean serializeNulls) {
        contextBuilder.serializeNulls(serializeNulls);
        return this;
    }
    
    @Override
    public DefaultConfig useFieldNames(boolean useFieldNames) {
        contextBuilder.useFieldNames(useFieldNames);
        return this;
    }
    
    @Override
    public DefaultConfig dateFormat(String dateFormat) {
        contextBuilder.dateFormat(dateFormat);
        return this;
    }
    
    @Override
    public DefaultConfig prettyPrinting(String indentation) {
        contextBuilder.prettyPrinting(indentation);
        return this;
    }
    
    @Override
    public DefaultConfig disablePrettyPrinting() {
        contextBuilder.disablePrettyPrinting();
        return this;
    }
    
    @Override
    public <A, B> DefaultConfig registerTypeAdapter(Class<A> type, TypeAdapter<A, B> adapter) {
        contextBuilder.registerTypeAdapter(type, adapter);
        return this;
    }
    
    @Override
    public SerializationContext createContext() {
        return contextBuilder.build();
    }
    
    /**
     * Gets the serializer factory associated with this configuration.
     *
     * @return The serializer factory
     */
    public SerializerFactory getSerializerFactory() {
        return serializerFactory;
    }
}