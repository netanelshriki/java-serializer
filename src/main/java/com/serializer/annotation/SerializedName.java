package com.serializer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that specifies the name to use for a field during serialization and deserialization.
 * <p>
 * This annotation allows customizing the field names in the serialized representation,
 * which can be useful for mapping between different naming conventions or for backward compatibility.
 * </p>
 * 
 * @author java-serializer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SerializedName {
    
    /**
     * The name to use for the field in the serialized representation.
     *
     * @return The serialized name
     */
    String value();
    
    /**
     * Alternative names for the field, used during deserialization.
     * <p>
     * This is useful for backward compatibility when field names change over time.
     * </p>
     *
     * @return Alternative names for the field
     */
    String[] alternate() default {};
}