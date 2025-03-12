package com.serializer.api;

/**
 * Interface for providing context during serialization and deserialization operations.
 * <p>
 * The serialization context maintains state and configuration settings that control
 * how objects are serialized and deserialized. It also provides access to type adapters
 * and other helper objects needed during these operations.
 * </p>
 * 
 * @author java-serializer
 */
public interface SerializationContext {
    
    /**
     * Gets a type adapter for the specified type.
     *
     * @param <T> The type for which to get an adapter
     * @param <R> The serialized representation type
     * @param type The class object representing the type
     * @return A type adapter for the specified type, or null if none exists
     */
    <T, R> TypeAdapter<T, R> getAdapter(Class<T> type);
    
    /**
     * Registers a type adapter for a specific type.
     *
     * @param <T> The type for which to register the adapter
     * @param <R> The serialized representation type
     * @param type The class object representing the type
     * @param adapter The type adapter to register
     * @return This context instance for method chaining
     */
    <T, R> SerializationContext registerAdapter(Class<T> type, TypeAdapter<T, R> adapter);
    
    /**
     * Gets the serializer factory associated with this context.
     *
     * @return The serializer factory
     */
    SerializerFactory getSerializerFactory();
    
    /**
     * Returns whether fields with null values should be included in serialization.
     *
     * @return true if null fields should be included, false otherwise
     */
    boolean isSerializeNulls();
    
    /**
     * Returns whether fields should be serialized using their declared names or custom names.
     *
     * @return true if field names should be used as-is, false if custom naming strategies should be applied
     */
    boolean isUseFieldNames();
    
    /**
     * Returns the date format pattern to use for serializing date and time objects.
     *
     * @return A string representing the date format pattern
     */
    String getDateFormat();
    
    /**
     * Returns the indentation string to use for pretty printing.
     *
     * @return The indentation string, or null if pretty printing is disabled
     */
    String getIndentation();
}