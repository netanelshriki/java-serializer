package com.serializer.api;

/**
 * Interface for configuring serialization and deserialization behavior.
 * <p>
 * This interface defines the contract for configuration objects that control
 * how serialization and deserialization operations are performed. Implementations
 * provide builder-style methods for setting various options.
 * </p>
 * 
 * @param <T> The specific type of configuration, for method chaining
 * @author java-serializer
 */
public interface Config<T extends Config<T>> {
    
    /**
     * Sets whether fields with null values should be included in serialization.
     *
     * @param serializeNulls true to include null fields, false to exclude them
     * @return This configuration instance for method chaining
     */
    T serializeNulls(boolean serializeNulls);
    
    /**
     * Sets whether fields should be serialized using their declared names.
     *
     * @param useFieldNames true to use field names as-is, false to apply naming strategies
     * @return This configuration instance for method chaining
     */
    T useFieldNames(boolean useFieldNames);
    
    /**
     * Sets the date format pattern to use for serializing date and time objects.
     *
     * @param dateFormat A string representing the date format pattern
     * @return This configuration instance for method chaining
     */
    T dateFormat(String dateFormat);
    
    /**
     * Enables pretty printing with the specified indentation.
     *
     * @param indentation The indentation string to use
     * @return This configuration instance for method chaining
     */
    T prettyPrinting(String indentation);
    
    /**
     * Disables pretty printing.
     *
     * @return This configuration instance for method chaining
     */
    T disablePrettyPrinting();
    
    /**
     * Registers a type adapter for a specific type.
     *
     * @param <A> The type for which to register the adapter
     * @param <B> The serialized representation type
     * @param type The class object representing the type
     * @param adapter The type adapter to register
     * @return This configuration instance for method chaining
     */
    <A, B> T registerTypeAdapter(Class<A> type, TypeAdapter<A, B> adapter);
    
    /**
     * Creates a new serialization context with the current configuration.
     *
     * @return A new serialization context
     */
    SerializationContext createContext();
}