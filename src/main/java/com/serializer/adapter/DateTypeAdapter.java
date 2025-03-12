package com.serializer.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.serializer.api.TypeAdapter;
import com.serializer.exception.DeserializationException;
import com.serializer.exception.SerializationException;
import com.serializer.exception.TypeAdapterException;

/**
 * Type adapter for converting between {@link Date} objects and strings.
 * <p>
 * This adapter serializes dates to strings using the specified date format
 * and deserializes strings back to dates.
 * </p>
 * 
 * @author java-serializer
 */
public class DateTypeAdapter implements TypeAdapter<Date, String> {
    
    private final SimpleDateFormat dateFormat;
    
    /**
     * Constructs a new date type adapter with the specified date format pattern.
     *
     * @param dateFormatPattern The date format pattern to use
     */
    public DateTypeAdapter(String dateFormatPattern) {
        this.dateFormat = new SimpleDateFormat(dateFormatPattern);
    }
    
    /**
     * Constructs a new date type adapter with the default ISO-8601 date format pattern.
     */
    public DateTypeAdapter() {
        this("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }
    
    @Override
    public String serialize(Date value) {
        if (value == null) {
            return null;
        }
        try {
            synchronized (dateFormat) {
                return dateFormat.format(value);
            }
        } catch (Exception e) {
            throw new SerializationException("Failed to format date: " + value, e);
        }
    }
    
    @Override
    public Date deserialize(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            synchronized (dateFormat) {
                return dateFormat.parse(value);
            }
        } catch (ParseException e) {
            throw new DeserializationException("Failed to parse date: " + value, e);
        }
    }
    
    @Override
    public Class<Date> getType() {
        return Date.class;
    }
}