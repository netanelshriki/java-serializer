package com.serializer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that specifies a custom type adapter to use for a field during serialization and deserialization.
 * <p>
 * This annotation allows for customized handling of specific fields that may require
 * special serialization or deserialization logic. The specified adapter class must
 * implement the {@link com.serializer.api.TypeAdapter} interface.
 * </p>
 * 
 * @author java-serializer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface TypeAdapter {
    
    /**
     * The type adapter class to use for the annotated field or type.
     *
     * @return The type adapter class
     */
    Class<? extends com.serializer.api.TypeAdapter<?, ?>> value();
}