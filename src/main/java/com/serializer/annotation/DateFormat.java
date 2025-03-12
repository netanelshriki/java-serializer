package com.serializer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that specifies the date format to use for a field during serialization and deserialization.
 * <p>
 * This annotation allows customizing how date and time fields are formatted in the
 * serialized representation. The format string follows the pattern syntax of
 * {@link java.text.SimpleDateFormat}.
 * </p>
 * 
 * @author java-serializer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DateFormat {
    
    /**
     * The date format pattern to use for the field.
     * <p>
     * For example, "yyyy-MM-dd HH:mm:ss" for dates like "2023-01-15 12:30:45".
     * </p>
     *
     * @return The date format pattern
     */
    String value();
    
    /**
     * The time zone ID to use for the date format.
     * <p>
     * For example, "UTC" or "America/New_York".
     * </p>
     *
     * @return The time zone ID, or an empty string to use the default time zone
     */
    String timeZone() default "";
}