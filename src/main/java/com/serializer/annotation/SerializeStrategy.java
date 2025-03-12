package com.serializer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that specifies the serialization strategy to use for a class.
 * <p>
 * This annotation allows customizing how an entire class is serialized and deserialized,
 * which can be useful for complex types or for applying consistent handling to all
 * instances of a particular class.
 * </p>
 * 
 * @author java-serializer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SerializeStrategy {
    
    /**
     * The serialization strategy class to use for the annotated type.
     * <p>
     * The specified class must implement the appropriate serializer/deserializer interfaces.
     * </p>
     *
     * @return The serialization strategy class
     */
    Class<?> value();
}