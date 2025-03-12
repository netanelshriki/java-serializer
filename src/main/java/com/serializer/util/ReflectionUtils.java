package com.serializer.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Date;
import java.time.temporal.Temporal;

/**
 * Utility class for reflection operations.
 * <p>
 * This class provides helper methods for common reflection tasks used during
 * serialization and deserialization, such as accessing fields and methods.
 * </p>
 * 
 * @author java-serializer
 */
public final class ReflectionUtils {
    
    private ReflectionUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Gets all declared fields from a class and its superclasses, excluding static and transient fields.
     * Handles module encapsulation for JDK classes like UUID.
     *
     * @param clazz The class to get fields from
     * @return A list of accessible fields
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        
        // Skip reflection for JDK classes that have module encapsulation
        if (isJdkClass(clazz)) {
            return fields;
        }
        
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                    try {
                        field.setAccessible(true);
                        fields.add(field);
                    } catch (Exception e) {
                        // Skip fields that can't be made accessible due to module encapsulation
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
    
    /**
     * Checks if a class is from the JDK and may have module encapsulation.
     * 
     * @param clazz The class to check
     * @return true if it's a JDK class with potential module encapsulation
     */
    private static boolean isJdkClass(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        
        String packageName = clazz.getPackageName();
        return packageName.startsWith("java.") || 
               packageName.startsWith("javax.") ||
               packageName.startsWith("sun.") ||
               clazz.equals(UUID.class) ||
               Number.class.isAssignableFrom(clazz) ||
               Date.class.isAssignableFrom(clazz) ||
               Temporal.class.isAssignableFrom(clazz);
    }
    
    /**
     * Gets all fields from a class annotated with a specific annotation.
     *
     * @param <A> The annotation type
     * @param clazz The class to get fields from
     * @param annotation The annotation class to look for
     * @return A list of fields with the specified annotation
     */
    public static <A extends Annotation> List<Field> getAnnotatedFields(Class<?> clazz, Class<A> annotation) {
        List<Field> annotatedFields = new ArrayList<>();
        for (Field field : getAllFields(clazz)) {
            if (field.isAnnotationPresent(annotation)) {
                annotatedFields.add(field);
            }
        }
        return annotatedFields;
    }
    
    /**
     * Gets a getter method for a field.
     *
     * @param clazz The class containing the field
     * @param fieldName The name of the field
     * @return The getter method, or null if none exists
     */
    public static Method getGetterMethod(Class<?> clazz, String fieldName) {
        String capitalizedName = capitalize(fieldName);
        try {
            return clazz.getMethod("get" + capitalizedName);
        } catch (NoSuchMethodException e) {
            try {
                return clazz.getMethod("is" + capitalizedName);
            } catch (NoSuchMethodException ex) {
                return null;
            }
        }
    }
    
    /**
     * Gets a setter method for a field.
     *
     * @param clazz The class containing the field
     * @param fieldName The name of the field
     * @param paramType The parameter type of the setter method
     * @return The setter method, or null if none exists
     */
    public static Method getSetterMethod(Class<?> clazz, String fieldName, Class<?> paramType) {
        String capitalizedName = capitalize(fieldName);
        try {
            return clazz.getMethod("set" + capitalizedName, paramType);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
    
    /**
     * Capitalizes the first letter of a string.
     *
     * @param str The string to capitalize
     * @return The capitalized string
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
    
    /**
     * Gets the value of a field from an object.
     * For JDK classes with module encapsulation, tries to use a getter method first.
     *
     * @param obj The object to get the field value from
     * @param field The field to get the value of
     * @return The field value
     * @throws IllegalArgumentException if the field cannot be accessed
     */
    public static Object getFieldValue(Object obj, Field field) {
        try {
            // First try to use a getter method if available
            Method getter = getGetterMethod(obj.getClass(), field.getName());
            if (getter != null) {
                return getter.invoke(obj);
            }
            
            // Fall back to direct field access
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            if (isJdkClass(field.getDeclaringClass())) {
                // For JDK classes, just return null instead of failing
                return null;
            }
            throw new IllegalArgumentException("Cannot access field: " + field.getName(), e);
        }
    }
    
    /**
     * Sets the value of a field in an object.
     * For JDK classes with module encapsulation, tries to use a setter method first.
     *
     * @param obj The object to set the field value in
     * @param field The field to set the value of
     * @param value The value to set
     * @throws IllegalArgumentException if the field cannot be accessed or the value is not compatible
     */
    public static void setFieldValue(Object obj, Field field, Object value) {
        try {
            // First try to use a setter method if available
            Method setter = getSetterMethod(obj.getClass(), field.getName(), field.getType());
            if (setter != null) {
                setter.invoke(obj, value);
                return;
            }
            
            // Fall back to direct field access
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            if (isJdkClass(field.getDeclaringClass())) {
                // For JDK classes, just return without failing
                return;
            }
            throw new IllegalArgumentException(
                    "Cannot set field " + field.getName() + " to value: " + value, e);
        }
    }
}