package com.serializer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that indicates a field should be ignored during serialization and deserialization.
 * <p>
 * This annotation provides a simple way to exclude fields from the serialization and
 * deserialization processes entirely. It is a convenience shorthand for {@code @Expose(serialize=false, deserialize=false)}.
 * </p>
 * 
 * @author java-serializer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonIgnore {
    // No properties needed, the presence of the annotation is enough
}