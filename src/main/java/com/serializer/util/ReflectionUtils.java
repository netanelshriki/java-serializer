package com.serializer.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
     *
     * @param clazz The class to get fields from
     * @return A list of accessible fields
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
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
     *
     * @param obj The object to get the field value from
     * @param field The field to get the value of
     * @return The field value
     * @throws IllegalArgumentException if the field cannot be accessed
     */
    public static Object getFieldValue(Object obj, Field field) {
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot access field: " + field.getName(), e);
        }
    }
    
    /**
     * Sets the value of a field in an object.
     *
     * @param obj The object to set the field value in
     * @param field The field to set the value of
     * @param value The value to set
     * @throws IllegalArgumentException if the field cannot be accessed or the value is not compatible
     */
    public static void setFieldValue(Object obj, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot access field: " + field.getName(), e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Cannot set field " + field.getName() + " to value: " + value, e);
        }
    }
}