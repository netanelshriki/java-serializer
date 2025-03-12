package com.serializer.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.serializer.annotation.Expose;
import com.serializer.annotation.JsonIgnore;
import com.serializer.annotation.SerializedName;
import com.serializer.api.SerializationContext;
import com.serializer.api.Serializer;
import com.serializer.api.TypeAdapter;
import com.serializer.exception.SerializationException;
import com.serializer.util.ReflectionUtils;
import com.serializer.util.StringUtils;

/**
 * Implementation of the {@link Serializer} interface for JSON serialization.
 * <p>
 * This class provides the ability to serialize Java objects into JSON format.
 * It handles common Java types and supports custom type adapters for complex types.
 * </p>
 * 
 * @param <T> The type of object to be serialized
 * @author java-serializer
 */
public class JsonSerializer<T> implements Serializer<T> {
    
    private final Class<T> type;
    private final SerializationContext context;
    
    /**
     * Constructs a new JSON serializer for the specified type and context.
     *
     * @param type The class of objects to serialize
     * @param context The serialization context to use
     */
    public JsonSerializer(Class<T> type, SerializationContext context) {
        this.type = type;
        this.context = context;
    }
    
    @Override
    public String serialize(T object) {
        if (object == null) {
            return "null";
        }
        
        StringBuilder sb = new StringBuilder();
        try {
            serializeToWriter(object, new java.io.StringWriter(), new JsonWriter(sb, context.getIndentation()));
            return sb.toString();
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize object of type " + type.getName(), e);
        }
    }
    
    @Override
    public void serialize(T object, Writer writer) {
        if (object == null) {
            try {
                writer.write("null");
            } catch (IOException e) {
                throw new SerializationException("Failed to write null", e);
            }
            return;
        }
        
        try {
            serializeToWriter(object, writer, new JsonWriter(writer, context.getIndentation()));
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize object of type " + type.getName(), e);
        }
    }
    
    @Override
    public void serialize(T object, OutputStream outputStream) {
        Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        serialize(object, writer);
        try {
            writer.flush();
        } catch (IOException e) {
            throw new SerializationException("Failed to flush writer", e);
        }
    }
    
    @Override
    public String getContentType() {
        return "application/json";
    }
    
    /**
     * Serializes an object and writes the JSON to the provided writer.
     *
     * @param object The object to serialize
     * @param out The writer to which the JSON will be written
     * @param jsonWriter The JSON writer to use
     * @throws Exception if serialization fails
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void serializeToWriter(Object object, Writer out, JsonWriter jsonWriter) throws Exception {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }
        
        Class<?> objectType = object.getClass();
        
        // Check for a type adapter first
        TypeAdapter typeAdapter = context.getAdapter(objectType);
        if (typeAdapter != null) {
            Object adapted = typeAdapter.serialize(object);
            serializeToWriter(adapted, out, jsonWriter);
            return;
        }
        
        // Handle primitive types and common Java types
        if (objectType.isPrimitive() || object instanceof Number || object instanceof Boolean) {
            jsonWriter.writeRaw(object.toString());
        } else if (object instanceof String) {
            jsonWriter.writeString((String) object);
        } else if (object instanceof Character) {
            jsonWriter.writeString(object.toString());
        } else if (object instanceof Date) {
            // Format date according to the configured date format
            jsonWriter.writeString(new java.text.SimpleDateFormat(context.getDateFormat())
                    .format((Date) object));
        } else if (objectType.isEnum()) {
            jsonWriter.writeString(((Enum<?>) object).name());
        } else if (objectType.isArray()) {
            serializeArray(object, jsonWriter);
        } else if (object instanceof Collection) {
            serializeCollection((Collection<?>) object, jsonWriter);
        } else if (object instanceof Map) {
            serializeMap((Map<?, ?>) object, jsonWriter);
        } else {
            // Complex object - serialize its fields
            serializeObject(object, jsonWriter);
        }
    }
    
    /**
     * Serializes an array to JSON.
     *
     * @param array The array to serialize
     * @param jsonWriter The JSON writer to use
     * @throws Exception if serialization fails
     */
    private void serializeArray(Object array, JsonWriter jsonWriter) throws Exception {
        jsonWriter.beginArray();
        
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                jsonWriter.writeArraySeparator();
            }
            Object element = Array.get(array, i);
            serializeToWriter(element, jsonWriter.getWriter(), jsonWriter);
        }
        
        jsonWriter.endArray();
    }
    
    /**
     * Serializes a collection to JSON.
     *
     * @param collection The collection to serialize
     * @param jsonWriter The JSON writer to use
     * @throws Exception if serialization fails
     */
    private void serializeCollection(Collection<?> collection, JsonWriter jsonWriter) throws Exception {
        jsonWriter.beginArray();
        
        boolean first = true;
        for (Object element : collection) {
            if (!first) {
                jsonWriter.writeArraySeparator();
            }
            first = false;
            serializeToWriter(element, jsonWriter.getWriter(), jsonWriter);
        }
        
        jsonWriter.endArray();
    }
    
    /**
     * Serializes a map to JSON.
     *
     * @param map The map to serialize
     * @param jsonWriter The JSON writer to use
     * @throws Exception if serialization fails
     */
    private void serializeMap(Map<?, ?> map, JsonWriter jsonWriter) throws Exception {
        jsonWriter.beginObject();
        
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            
            // Skip null values if configured to do so
            if (value == null && !context.isSerializeNulls()) {
                continue;
            }
            
            if (!first) {
                jsonWriter.writeObjectSeparator();
            }
            first = false;
            
            // Keys must be strings in JSON
            jsonWriter.writeName(key.toString());
            serializeToWriter(value, jsonWriter.getWriter(), jsonWriter);
        }
        
        jsonWriter.endObject();
    }
    
    /**
     * Serializes a complex object to JSON by serializing its fields.
     *
     * @param object The object to serialize
     * @param jsonWriter The JSON writer to use
     * @throws Exception if serialization fails
     */
    private void serializeObject(Object object, JsonWriter jsonWriter) throws Exception {
        jsonWriter.beginObject();
        
        Class<?> objectType = object.getClass();
        List<Field> fields = ReflectionUtils.getAllFields(objectType);
        
        boolean first = true;
        for (Field field : fields) {
            // Skip fields annotated with @JsonIgnore
            if (field.isAnnotationPresent(JsonIgnore.class)) {
                continue;
            }
            
            // Check @Expose annotation
            Expose expose = field.getAnnotation(Expose.class);
            if (expose != null && !expose.serialize()) {
                continue;
            }
            
            // Get field value
            Object value = ReflectionUtils.getFieldValue(object, field);
            
            // Skip null values if configured to do so
            if (value == null && !context.isSerializeNulls()) {
                continue;
            }
            
            if (!first) {
                jsonWriter.writeObjectSeparator();
            }
            first = false;
            
            // Get field name, using @SerializedName if present
            String fieldName = field.getName();
            SerializedName serializedName = field.getAnnotation(SerializedName.class);
            if (serializedName != null) {
                fieldName = serializedName.value();
            } else if (!context.isUseFieldNames()) {
                // Apply naming strategy if not using field names directly
                fieldName = StringUtils.camelToSnakeCase(fieldName);
            }
            
            jsonWriter.writeName(fieldName);
            
            // Check for field-specific type adapter
            TypeAdapter<?, ?> fieldAdapter = context.getAdapter(field.getType());
            if (fieldAdapter != null && value != null) {
                Object adapted = fieldAdapter.serialize(value);
                serializeToWriter(adapted, jsonWriter.getWriter(), jsonWriter);
            } else {
                serializeToWriter(value, jsonWriter.getWriter(), jsonWriter);
            }
        }
        
        jsonWriter.endObject();
    }
    
    /**
     * Helper class for writing JSON with proper formatting.
     */
    private static class JsonWriter {
        private final Writer writer;
        private final StringBuilder stringBuilder;
        private final String indentation;
        private int indentLevel = 0;
        
        /**
         * Constructs a new JSON writer that writes to a string builder.
         *
         * @param stringBuilder The string builder to write to
         * @param indentation The indentation string, or null to disable pretty printing
         */
        public JsonWriter(StringBuilder stringBuilder, String indentation) {
            this.writer = null;
            this.stringBuilder = stringBuilder;
            this.indentation = indentation;
        }
        
        /**
         * Constructs a new JSON writer that writes to a writer.
         *
         * @param writer The writer to write to
         * @param indentation The indentation string, or null to disable pretty printing
         */
        public JsonWriter(Writer writer, String indentation) {
            this.writer = writer;
            this.stringBuilder = null;
            this.indentation = indentation;
        }
        
        /**
         * Gets the underlying writer.
         *
         * @return The writer
         */
        public Writer getWriter() {
            return writer;
        }
        
        /**
         * Writes a string to the output.
         *
         * @param string The string to write
         * @throws IOException if writing fails
         */
        public void writeString(String string) throws IOException {
            write('"');
            write(StringUtils.escapeJson(string));
            write('"');
        }
        
        /**
         * Writes a property name to the output.
         *
         * @param name The property name to write
         * @throws IOException if writing fails
         */
        public void writeName(String name) throws IOException {
            writeString(name);
            write(':');
            if (isPrettyPrinting()) {
                write(' ');
            }
        }
        
        /**
         * Writes a null value to the output.
         *
         * @throws IOException if writing fails
         */
        public void writeNull() throws IOException {
            writeRaw("null");
        }
        
        /**
         * Writes a raw string to the output without any escaping or quoting.
         *
         * @param raw The raw string to write
         * @throws IOException if writing fails
         */
        public void writeRaw(String raw) throws IOException {
            write(raw);
        }
        
        /**
         * Begins a JSON object.
         *
         * @throws IOException if writing fails
         */
        public void beginObject() throws IOException {
            write('{');
            indentLevel++;
            writeNewLineAndIndent();
        }
        
        /**
         * Ends a JSON object.
         *
         * @throws IOException if writing fails
         */
        public void endObject() throws IOException {
            indentLevel--;
            writeNewLineAndIndent();
            write('}');
        }
        
        /**
         * Begins a JSON array.
         *
         * @throws IOException if writing fails
         */
        public void beginArray() throws IOException {
            write('[');
            indentLevel++;
            writeNewLineAndIndent();
        }
        
        /**
         * Ends a JSON array.
         *
         * @throws IOException if writing fails
         */
        public void endArray() throws IOException {
            indentLevel--;
            writeNewLineAndIndent();
            write(']');
        }
        
        /**
         * Writes a separator between object properties.
         *
         * @throws IOException if writing fails
         */
        public void writeObjectSeparator() throws IOException {
            write(',');
            writeNewLineAndIndent();
        }
        
        /**
         * Writes a separator between array elements.
         *
         * @throws IOException if writing fails
         */
        public void writeArraySeparator() throws IOException {
            write(',');
            writeNewLineAndIndent();
        }
        
        /**
         * Writes a character to the output.
         *
         * @param c The character to write
         * @throws IOException if writing fails
         */
        private void write(char c) throws IOException {
            if (writer != null) {
                writer.write(c);
            } else {
                stringBuilder.append(c);
            }
        }
        
        /**
         * Writes a string to the output.
         *
         * @param s The string to write
         * @throws IOException if writing fails
         */
        private void write(String s) throws IOException {
            if (writer != null) {
                writer.write(s);
            } else {
                stringBuilder.append(s);
            }
        }
        
        /**
         * Writes a new line and indentation if pretty printing is enabled.
         *
         * @throws IOException if writing fails
         */
        private void writeNewLineAndIndent() throws IOException {
            if (isPrettyPrinting()) {
                write('\n');
                for (int i = 0; i < indentLevel; i++) {
                    write(indentation);
                }
            }
        }
        
        /**
         * Returns whether pretty printing is enabled.
         *
         * @return true if pretty printing is enabled, false otherwise
         */
        private boolean isPrettyPrinting() {
            return indentation != null;
        }
    }
}