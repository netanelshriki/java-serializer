package com.serializer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that indicates whether a field should be exposed for serialization and/or deserialization.
 * <p>
 * This annotation provides fine-grained control over which fields are included in the
 * serialization and deserialization processes. Fields that are not exposed are ignored.
 * </p>
 * 
 * @author java-serializer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Expose {
    
    /**
     * Indicates whether the field should be included in serialization.
     *
     * @return true if the field should be serialized, false otherwise
     */
    boolean serialize() default true;
    
    /**
     * Indicates whether the field should be included in deserialization.
     *
     * @return true if the field should be deserialized, false otherwise
     */
    boolean deserialize() default true;
}