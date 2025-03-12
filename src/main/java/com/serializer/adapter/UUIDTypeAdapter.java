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
        
        // Handle quoted string format if present
        String uuidString = value;
        if (uuidString.startsWith("\"") && uuidString.endsWith("\"")) {
            uuidString = uuidString.substring(1, uuidString.length() - 1);
        }
        
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            // Try to create a UUID from a different format or attempt to repair common issues
            try {
                // If it's not in the standard format, try to normalize it
                uuidString = uuidString.trim().toLowerCase()
                        .replace(" ", "")
                        .replace("{", "")
                        .replace("}", "");
                
                // Check if we need to add hyphens for a UUID with no separators
                if (!uuidString.contains("-") && uuidString.length() == 32) {
                    uuidString = uuidString.substring(0, 8) + "-" + 
                                 uuidString.substring(8, 12) + "-" + 
                                 uuidString.substring(12, 16) + "-" + 
                                 uuidString.substring(16, 20) + "-" + 
                                 uuidString.substring(20);
                    return UUID.fromString(uuidString);
                }
                
                // If all attempts fail, throw the original exception
                throw new DeserializationException("Invalid UUID: " + value, e);
            } catch (Exception ex) {
                throw new DeserializationException("Invalid UUID: " + value, e);
            }
        }
    }
    
    @Override
    public Class<UUID> getType() {
        return UUID.class;
    }
}