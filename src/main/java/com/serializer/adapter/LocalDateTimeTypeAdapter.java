package com.serializer.adapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.serializer.api.TypeAdapter;
import com.serializer.exception.DeserializationException;
import com.serializer.exception.SerializationException;
import com.serializer.exception.TypeAdapterException;

/**
 * Type adapter for converting between {@link LocalDateTime} objects and strings.
 * <p>
 * This adapter serializes LocalDateTime objects to strings using the specified date format
 * and deserializes strings back to LocalDateTime objects.
 * </p>
 * 
 * @author java-serializer
 */
public class LocalDateTimeTypeAdapter implements TypeAdapter<LocalDateTime, String> {
    
    private final DateTimeFormatter formatter;
    
    /**
     * Constructs a new LocalDateTime type adapter with the specified date format pattern.
     *
     * @param pattern The date format pattern to use
     */
    public LocalDateTimeTypeAdapter(String pattern) {
        this.formatter = DateTimeFormatter.ofPattern(pattern);
    }
    
    /**
     * Constructs a new LocalDateTime type adapter with the default ISO-8601 date format pattern.
     */
    public LocalDateTimeTypeAdapter() {
        this.formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    }
    
    @Override
    public String serialize(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        try {
            return formatter.format(value);
        } catch (Exception e) {
            throw new SerializationException("Failed to format LocalDateTime: " + value, e);
        }
    }
    
    @Override
    public LocalDateTime deserialize(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, formatter);
        } catch (DateTimeParseException e) {
            throw new DeserializationException("Failed to parse LocalDateTime: " + value, e);
        }
    }
    
    @Override
    public Class<LocalDateTime> getType() {
        return LocalDateTime.class;
    }
}