package com.serializer.adapter;

import java.util.UUID;

import com.serializer.api.TypeAdapter;
import com.serializer.exception.DeserializationException;
import com.serializer.exception.SerializationException;

/**
 * Type adapter for converting between {@link UUID} objects and strings.
 * <p>
 * This adapter serializes UUID objects to their string representation
 * and deserializes strings back to UUID objects.
 * </p>
 * 
 * @author java-serializer
 */
public class UUIDTypeAdapter implements TypeAdapter<UUID, String> {
    
    @Override
    public String serialize(UUID value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
    
    @Override
    public UUID deserialize(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new DeserializationException("Invalid UUID: " + value, e);
        }
    }
    
    @Override
    public Class<UUID> getType() {
        return UUID.class;
    }
}