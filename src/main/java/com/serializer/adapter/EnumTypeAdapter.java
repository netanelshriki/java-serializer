package com.serializer.adapter;

import com.serializer.api.TypeAdapter;
import com.serializer.exception.DeserializationException;
import com.serializer.exception.TypeAdapterException;

/**
 * Type adapter for converting between enum constants and strings.
 * <p>
 * This adapter serializes enum constants to their names and deserializes
 * strings back to enum constants.
 * </p>
 * 
 * @param <E> The enum type
 * @author java-serializer
 */
public class EnumTypeAdapter<E extends Enum<E>> implements TypeAdapter<E, String> {
    
    private final Class<E> enumType;
    
    /**
     * Constructs a new enum type adapter for the specified enum type.
     *
     * @param enumType The enum class
     */
    public EnumTypeAdapter(Class<E> enumType) {
        this.enumType = enumType;
    }
    
    @Override
    public String serialize(E value) {
        if (value == null) {
            return null;
        }
        return value.name();
    }
    
    @Override
    public E deserialize(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException e) {
            // Try to handle if the value is quoted
            if (value.startsWith("\"") && value.endsWith("\"")) {
                String unquoted = value.substring(1, value.length() - 1);
                try {
                    return Enum.valueOf(enumType, unquoted);
                } catch (IllegalArgumentException ex) {
                    // Ignore and continue to the next case
                }
            }
            
            // Try case-insensitive match
            for (E enumConstant : enumType.getEnumConstants()) {
                if (enumConstant.name().equalsIgnoreCase(value)) {
                    return enumConstant;
                }
            }
            
            throw new DeserializationException("Invalid enum value: " + value + " for enum " + enumType.getName(), e);
        }
    }
    
    @Override
    public Class<E> getType() {
        return enumType;
    }
}