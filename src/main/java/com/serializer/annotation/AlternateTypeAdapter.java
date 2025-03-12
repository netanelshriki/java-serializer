package com.serializer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that specifies multiple type adapters to try in sequence for a field during deserialization.
 * <p>
 * This annotation is useful for backward compatibility when the format of a field changes over time.
 * During deserialization, each specified adapter will be tried in order until one succeeds.
 * </p>
 * 
 * @author java-serializer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface AlternateTypeAdapter {
    
    /**
     * The type adapter classes to try in sequence during deserialization.
     *
     * @return An array of type adapter classes
     */
    Class<? extends com.serializer.api.TypeAdapter<?, ?>>[] value();
}