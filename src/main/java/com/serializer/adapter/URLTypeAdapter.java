package com.serializer.adapter;

import java.net.MalformedURLException;
import java.net.URL;

import com.serializer.api.TypeAdapter;
import com.serializer.exception.DeserializationException;
import com.serializer.exception.SerializationException;

/**
 * Type adapter for converting between {@link URL} objects and strings.
 * <p>
 * This adapter serializes URL objects to their string representation
 * and deserializes strings back to URL objects.
 * </p>
 * 
 * @author java-serializer
 */
public class URLTypeAdapter implements TypeAdapter<URL, String> {
    
    @Override
    public String serialize(URL value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
    
    @Override
    public URL deserialize(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return new URL(value);
        } catch (MalformedURLException e) {
            throw new DeserializationException("Invalid URL: " + value, e);
        }
    }
    
    @Override
    public Class<URL> getType() {
        return URL.class;
    }
}