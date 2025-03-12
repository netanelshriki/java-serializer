package com.serializer.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.serializer.annotation.Expose;
import com.serializer.annotation.JsonIgnore;
import com.serializer.annotation.SerializedName;
import com.serializer.api.Deserializer;
import com.serializer.api.SerializationContext;
import com.serializer.api.TypeAdapter;
import com.serializer.exception.DeserializationException;
import com.serializer.util.ReflectionUtils;
import com.serializer.util.StringUtils;

/**
 * Implementation of the {@link Deserializer} interface for JSON deserialization.
 * <p>
 * This class provides the ability to deserialize JSON into Java objects.
 * It handles common Java types and supports custom type adapters for complex types.
 * </p>
 * 
 * @param <T> The type of object to deserialize into
 * @author java-serializer
 */
public class JsonDeserializer<T> implements Deserializer<T> {
    
    private final Class<T> type;
    private final SerializationContext context;
    
    /**
     * Constructs a new JSON deserializer for the specified type and context.
     *
     * @param type The class of objects to deserialize into
     * @param context The serialization context to use
     */
    public JsonDeserializer(Class<T> type, SerializationContext context) {
        this.type = type;
        this.context = context;
    }
    
    @Override
    public T deserialize(String serializedData) {
        if (serializedData == null || serializedData.trim().equals("null")) {
            return null;
        }
        
        try {
            JsonParser parser = new JsonParser(serializedData);
            Object value = parseValue(parser);
            return convertToTargetType(value);
        } catch (Exception e) {
            throw new DeserializationException("Failed to deserialize JSON to type " + type.getName(), e);
        }
    }
    
    @Override
    public T deserialize(Reader reader) {
        try {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, charsRead);
            }
            return deserialize(sb.toString());
        } catch (IOException e) {
            throw new DeserializationException("Failed to read JSON from reader", e);
        }
    }
    
    @Override
    public T deserialize(InputStream inputStream) {
        return deserialize(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }
    
    @Override
    public Class<T> getTargetClass() {
        return type;
    }
    
    /**
     * Parses a JSON value from the parser.
     *
     * @param parser The JSON parser to use
     * @return The parsed value
     * @throws Exception if parsing fails
     */
    private Object parseValue(JsonParser parser) throws Exception {
        parser.skipWhitespace();
        
        char c = parser.peek();
        switch (c) {
            case '{':
                return parseObject(parser);
            case '[':
                return parseArray(parser);
            case '"':
                return parser.nextString();
            case 't':
                parser.expect("true");
                return Boolean.TRUE;
            case 'f':
                parser.expect("false");
                return Boolean.FALSE;
            case 'n':
                parser.expect("null");
                return null;
            default:
                if (c == '-' || Character.isDigit(c)) {
                    return parser.nextNumber();
                }
                throw new DeserializationException("Unexpected character in JSON: " + c);
        }
    }
    
    /**
     * Parses a JSON object from the parser.
     *
     * @param parser The JSON parser to use
     * @return The parsed object as a map
     * @throws Exception if parsing fails
     */
    private Map<String, Object> parseObject(JsonParser parser) throws Exception {
        Map<String, Object> map = new LinkedHashMap<>();
        
        parser.expect('{');
        parser.skipWhitespace();
        
        if (parser.peek() == '}') {
            parser.next();
            return map;
        }
        
        while (true) {
            parser.skipWhitespace();
            String key = parser.nextString();
            
            parser.skipWhitespace();
            parser.expect(':');
            
            Object value = parseValue(parser);
            map.put(key, value);
            
            parser.skipWhitespace();
            char next = parser.next();
            if (next == '}') {
                break;
            } else if (next != ',') {
                throw new DeserializationException("Expected ',' or '}' in object, got '" + next + "'");
            }
        }
        
        return map;
    }
    
    /**
     * Parses a JSON array from the parser.
     *
     * @param parser The JSON parser to use
     * @return The parsed array as a list
     * @throws Exception if parsing fails
     */
    private List<Object> parseArray(JsonParser parser) throws Exception {
        List<Object> list = new ArrayList<>();
        
        parser.expect('[');
        parser.skipWhitespace();
        
        if (parser.peek() == ']') {
            parser.next();
            return list;
        }
        
        while (true) {
            Object value = parseValue(parser);
            list.add(value);
            
            parser.skipWhitespace();
            char next = parser.next();
            if (next == ']') {
                break;
            } else if (next != ',') {
                throw new DeserializationException("Expected ',' or ']' in array, got '" + next + "'");
            }
            parser.skipWhitespace();
        }
        
        return list;
    }
    
    /**
     * Converts a parsed JSON value to the target type.
     *
     * @param <R> The target type
     * @param value The parsed JSON value
     * @return An instance of the target type
     * @throws Exception if conversion fails
     */
    @SuppressWarnings("unchecked")
    private <R> R convertToTargetType(Object value) throws Exception {
        if (value == null) {
            return null;
        }
        
        // Special handling for UUID
        if (type == UUID.class && value instanceof String) {
            return (R) UUID.fromString((String) value);
        }
        
        // Check for a type adapter first
        TypeAdapter<T, Object> typeAdapter = (TypeAdapter<T, Object>) context.getAdapter(type);
        if (typeAdapter != null) {
            return (R) typeAdapter.deserialize(value);
        }
        
        // Handle primitive types and common Java types
        if (type.isPrimitive() || Number.class.isAssignableFrom(type) || Boolean.class == type) {
            return convertPrimitive(value, type);
        } else if (String.class == type) {
            return (R) value.toString();
        } else if (Character.class == type) {
            String str = value.toString();
            return (R) (str.isEmpty() ? null : Character.valueOf(str.charAt(0)));
        } else if (Date.class.isAssignableFrom(type)) {
            return (R) convertDate(value);
        } else if (type.isEnum()) {
            return (R) convertEnum(value, (Class<Enum>) type);
        } else if (type.isArray()) {
            if (value instanceof List) {
                return (R) convertArray(value, type.getComponentType());
            } else {
                // If we received a single value but need an array, create a single-element array
                Object array = Array.newInstance(type.getComponentType(), 1);
                Array.set(array, 0, convertValue(value, type.getComponentType(), null));
                return (R) array;
            }
        } else if (Collection.class.isAssignableFrom(type)) {
            if (value instanceof List) {
                return (R) convertCollection(value, type);
            } else {
                // If we received a single value but need a collection, create a single-element collection
                Collection collection = createCollection(type);
                collection.add(value);
                return (R) collection;
            }
        } else if (Map.class.isAssignableFrom(type)) {
            if (value instanceof Map) {
                return (R) convertMap(value, type);
            } else {
                throw new DeserializationException("Expected object for Map type, got " + value.getClass().getName());
            }
        } else {
            // Complex object - deserialize its fields
            if (value instanceof Map) {
                return (R) convertObject(value, type);
            } else if (value instanceof Number || value instanceof Boolean || value instanceof String) {
                // Handle case where a primitive value is received but a complex object is expected
                // Try to find a single-argument constructor or static factory method
                try {
                    return (R) createObjectFromPrimitive(value, type);
                } catch (Exception e) {
                    throw new DeserializationException("Expected object, got " + value.getClass().getName(), e);
                }
            } else {
                throw new DeserializationException("Expected object, got " + value.getClass().getName());
            }
        }
    }

    /**
     * Creates a collection instance of the specified type.
     *
     * @param collectionType The collection class
     * @return A new collection instance
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection createCollection(Class<?> collectionType) {
        if (collectionType.isInterface()) {
            if (List.class.isAssignableFrom(collectionType)) {
                return new ArrayList();
            } else if (java.util.Set.class.isAssignableFrom(collectionType)) {
                return new java.util.LinkedHashSet();
            } else {
                return new ArrayList();
            }
        } else {
            try {
                return (Collection) collectionType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                return new ArrayList();
            }
        }
    }
    
    /**
     * Attempts to create an object from a primitive value using constructors or factory methods.
     *
     * @param <R> The target type
     * @param value The primitive value
     * @param targetType The target class
     * @return An instance of the target type
     * @throws Exception if creation fails
     */
    @SuppressWarnings({"unchecked"})
    private <R> R createObjectFromPrimitive(Object value, Class<?> targetType) throws Exception {
        // Try to use a constructor that takes the value's type
        try {
            if (value instanceof String) {
                return (R) targetType.getDeclaredConstructor(String.class).newInstance(value);
            } else if (value instanceof Number) {
                // Try different numeric constructors
                if (value instanceof Integer) {
                    return (R) targetType.getDeclaredConstructor(int.class).newInstance(((Number) value).intValue());
                } else if (value instanceof Long) {
                    return (R) targetType.getDeclaredConstructor(long.class).newInstance(((Number) value).longValue());
                } else if (value instanceof Double) {
                    return (R) targetType.getDeclaredConstructor(double.class).newInstance(((Number) value).doubleValue());
                }
            } else if (value instanceof Boolean) {
                return (R) targetType.getDeclaredConstructor(boolean.class).newInstance(value);
            }
        } catch (NoSuchMethodException ignored) {
            // Fall through to try other approaches
        }
        
        // Try static factory methods like valueOf or fromString
        try {
            if (value instanceof String) {
                return (R) targetType.getMethod("valueOf", String.class).invoke(null, value);
            } else if (value instanceof Number) {
                // Try different valueOf methods
                if (value instanceof Integer) {
                    return (R) targetType.getMethod("valueOf", int.class).invoke(null, ((Number) value).intValue());
                } else if (value instanceof Long) {
                    return (R) targetType.getMethod("valueOf", long.class).invoke(null, ((Number) value).longValue());
                } else if (value instanceof Double) {
                    return (R) targetType.getMethod("valueOf", double.class).invoke(null, ((Number) value).doubleValue());
                }
            }
        } catch (NoSuchMethodException ignored) {
            // Fall through to default behavior
        }
        
        // If all else fails, create an empty object and try to set a field with the value's name
        Object obj = targetType.getDeclaredConstructor().newInstance();
        for (Field field : ReflectionUtils.getAllFields(targetType)) {
            if (field.getName().equalsIgnoreCase("value") || 
                field.getName().equalsIgnoreCase("id") || 
                field.getName().equalsIgnoreCase("name")) {
                ReflectionUtils.setFieldValue(obj, field, value);
                return (R) obj;
            }
        }
        
        return (R) obj;
    }
    
    /**
     * Converts a parsed JSON value to a primitive or wrapper type.
     *
     * @param <R> The target type
     * @param value The parsed JSON value
     * @param targetType The target class
     * @return An instance of the target type
     * @throws Exception if conversion fails
     */
    @SuppressWarnings("unchecked")
    private <R> R convertPrimitive(Object value, Class<?> targetType) throws Exception {
        if (value == null) {
            return null;
        }
        
        if (targetType == int.class || targetType == Integer.class) {
            if (value instanceof Number) {
                return (R) Integer.valueOf(((Number) value).intValue());
            }
            return (R) Integer.valueOf(value.toString());
        } else if (targetType == long.class || targetType == Long.class) {
            if (value instanceof Number) {
                return (R) Long.valueOf(((Number) value).longValue());
            }
            return (R) Long.valueOf(value.toString());
        } else if (targetType == double.class || targetType == Double.class) {
            if (value instanceof Number) {
                return (R) Double.valueOf(((Number) value).doubleValue());
            }
            return (R) Double.valueOf(value.toString());
        } else if (targetType == float.class || targetType == Float.class) {
            if (value instanceof Number) {
                return (R) Float.valueOf(((Number) value).floatValue());
            }
            return (R) Float.valueOf(value.toString());
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            if (value instanceof Boolean) {
                return (R) value;
            }
            return (R) Boolean.valueOf(value.toString());
        } else if (targetType == byte.class || targetType == Byte.class) {
            if (value instanceof Number) {
                return (R) Byte.valueOf(((Number) value).byteValue());
            }
            return (R) Byte.valueOf(value.toString());
        } else if (targetType == short.class || targetType == Short.class) {
            if (value instanceof Number) {
                return (R) Short.valueOf(((Number) value).shortValue());
            }
            return (R) Short.valueOf(value.toString());
        } else if (targetType == char.class || targetType == Character.class) {
            String str = value.toString();
            return (R) (str.isEmpty() ? null : Character.valueOf(str.charAt(0)));
        }
        
        throw new DeserializationException("Cannot convert " + value + " to " + targetType.getName());
    }
    
    /**
     * Converts a parsed JSON value to a date.
     *
     * @param value The parsed JSON value
     * @return A date object
     * @throws Exception if conversion fails
     */
    private Date convertDate(Object value) throws Exception {
        if (value == null) {
            return null;
        }
        
        String dateStr = value.toString();
        try {
            return new SimpleDateFormat(context.getDateFormat()).parse(dateStr);
        } catch (ParseException e) {
            throw new DeserializationException("Cannot parse date: " + dateStr, e);
        }
    }
    
    /**
     * Converts a parsed JSON value to an enum.
     *
     * @param <E> The enum type
     * @param value The parsed JSON value
     * @param enumType The enum class
     * @return An enum constant
     * @throws Exception if conversion fails
     */
    private <E extends Enum<E>> E convertEnum(Object value, Class<E> enumType) throws Exception {
        if (value == null) {
            return null;
        }
        
        String enumStr = value.toString();
        try {
            return Enum.valueOf(enumType, enumStr);
        } catch (IllegalArgumentException e) {
            throw new DeserializationException("Cannot convert " + enumStr + " to enum " + enumType.getName(), e);
        }
    }
    
    /**
     * Converts a parsed JSON value to an array.
     *
     * @param value The parsed JSON value
     * @param componentType The component type of the array
     * @return An array of the specified component type
     * @throws Exception if conversion fails
     */
    private Object convertArray(Object value, Class<?> componentType) throws Exception {
        if (value == null) {
            return null;
        }
        
        if (!(value instanceof List)) {
            throw new DeserializationException("Expected array, got " + value.getClass().getName());
        }
        
        List<?> list = (List<?>) value;
        Object array = Array.newInstance(componentType, list.size());
        
        for (int i = 0; i < list.size(); i++) {
            Object element = list.get(i);
            Object convertedElement = convertValue(element, componentType, null);
            Array.set(array, i, convertedElement);
        }
        
        return array;
    }
    
    /**
     * Converts a parsed JSON value to a collection.
     *
     * @param <C> The collection type
     * @param value The parsed JSON value
     * @param collectionType The collection class
     * @return A collection of the specified type
     * @throws Exception if conversion fails
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <C extends Collection<?>> C convertCollection(Object value, Class<?> collectionType) throws Exception {
        if (value == null) {
            return null;
        }
        
        if (!(value instanceof List)) {
            throw new DeserializationException("Expected array, got " + value.getClass().getName());
        }
        
        List<?> list = (List<?>) value;
        Collection collection = createCollection(collectionType);
        
        // Determine the element type
        Class<?> elementType = Object.class;
        Type genericType = type.getGenericSuperclass();
        if (genericType instanceof ParameterizedType) {
            Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
            if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                elementType = (Class<?>) typeArgs[0];
            }
        }
        
        // Add elements to the collection
        for (Object element : list) {
            collection.add(convertValue(element, elementType, null));
        }
        
        return (C) collection;
    }
    
    /**
     * Converts a parsed JSON value to a map.
     *
     * @param <M> The map type
     * @param value The parsed JSON value
     * @param mapType The map class
     * @return A map of the specified type
     * @throws Exception if conversion fails
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <M extends Map<?, ?>> M convertMap(Object value, Class<?> mapType) throws Exception {
        if (value == null) {
            return null;
        }
        
        if (!(value instanceof Map)) {
            throw new DeserializationException("Expected object, got " + value.getClass().getName());
        }
        
        Map<?, ?> sourceMap = (Map<?, ?>) value;
        Map map;
        
        // Create a new instance of the map type
        if (mapType.isInterface()) {
            map = new HashMap();
        } else {
            try {
                map = (Map) mapType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new DeserializationException("Cannot create instance of " + mapType.getName(), e);
            }
        }
        
        // Determine the key and value types
        Class<?> keyType = String.class;
        Class<?> valueType = Object.class;
        Type genericType = type.getGenericSuperclass();
        if (genericType instanceof ParameterizedType) {
            Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
            if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                keyType = (Class<?>) typeArgs[0];
            }
            if (typeArgs.length > 1 && typeArgs[1] instanceof Class) {
                valueType = (Class<?>) typeArgs[1];
            }
        }
        
        // Add entries to the map
        for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
            Object key = convertValue(entry.getKey().toString(), keyType, null);
            Object mapValue = convertValue(entry.getValue(), valueType, null);
            map.put(key, mapValue);
        }
        
        return (M) map;
    }
    
    /**
     * Converts a parsed JSON value to a complex object.
     *
     * @param <O> The object type
     * @param value The parsed JSON value
     * @param objectType The object class
     * @return An instance of the specified type
     * @throws Exception if conversion fails
     */
    @SuppressWarnings("unchecked")
    private <O> O convertObject(Object value, Class<O> objectType) throws Exception {
        if (value == null) {
            return null;
        }
        
        if (!(value instanceof Map)) {
            throw new DeserializationException("Expected object, got " + value.getClass().getName());
        }
        
        Map<String, Object> map = (Map<String, Object>) value;
        O instance;
        
        // Create a new instance of the object type
        try {
            instance = objectType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new DeserializationException("Cannot create instance of " + objectType.getName(), e);
        }
        
        // Build a map of field names to fields
        Map<String, Field> fieldMap = new HashMap<>();
        Map<String, Field> serializedNameMap = new HashMap<>();
        
        for (Field field : ReflectionUtils.getAllFields(objectType)) {
            // Skip fields annotated with @JsonIgnore
            if (field.isAnnotationPresent(JsonIgnore.class)) {
                continue;
            }
            
            // Check @Expose annotation
            Expose expose = field.getAnnotation(Expose.class);
            if (expose != null && !expose.deserialize()) {
                continue;
            }
            
            // Get field name, using @SerializedName if present
            String fieldName = field.getName();
            SerializedName serializedName = field.getAnnotation(SerializedName.class);
            
            if (serializedName != null) {
                String primaryName = serializedName.value();
                serializedNameMap.put(primaryName, field);
                
                for (String alternateName : serializedName.alternate()) {
                    serializedNameMap.put(alternateName, field);
                }
            } else if (!context.isUseFieldNames()) {
                // Apply naming strategy if not using field names directly
                String snakeCaseName = StringUtils.camelToSnakeCase(fieldName);
                fieldMap.put(snakeCaseName, field);
            }
            
            fieldMap.put(fieldName, field);
        }
        
        // Set field values
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object mapValue = entry.getValue();
            
            // Find the field for this key
            Field field = serializedNameMap.get(key);
            if (field == null) {
                field = fieldMap.get(key);
            }
            
            if (field != null) {
                // Convert the value to the field type
                Object convertedValue = convertValue(mapValue, field.getType(), field);
                ReflectionUtils.setFieldValue(instance, field, convertedValue);
            }
        }
        
        return instance;
    }
    
    /**
     * Converts a value to the specified type, using field-specific type adapters if available.
     *
     * @param <V> The target type
     * @param value The value to convert
     * @param targetType The target class
     * @param field The field the value will be assigned to, or null if not applicable
     * @return An instance of the target type
     * @throws Exception if conversion fails
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <V> V convertValue(Object value, Class<V> targetType, Field field) throws Exception {
        if (value == null) {
            return null;
        }
        
        // Check for field-specific type adapter
        if (field != null) {
            com.serializer.annotation.TypeAdapter adapterAnnotation = field.getAnnotation(com.serializer.annotation.TypeAdapter.class);
            if (adapterAnnotation != null) {
                try {
                    TypeAdapter adapter = adapterAnnotation.value().getDeclaredConstructor().newInstance();
                    return (V) adapter.deserialize(value);
                } catch (Exception e) {
                    throw new DeserializationException("Failed to use type adapter for field " + field.getName(), e);
                }
            }
        }
        
        // Check for type-level adapter
        TypeAdapter typeAdapter = context.getAdapter(targetType);
        if (typeAdapter != null) {
            return (V) typeAdapter.deserialize(value);
        }
        
        // Use generic conversion
        if (targetType.isAssignableFrom(value.getClass())) {
            return (V) value;
        } else {
            // For classes with the same wrapper type (e.g., Integer, Long, Double),
            // convert between them if possible
            if (Number.class.isAssignableFrom(targetType) && value instanceof Number) {
                return convertPrimitive(value, targetType);
            }
            
            // For special JDK classes with type issues
            if (targetType == UUID.class && value instanceof String) {
                return (V) UUID.fromString((String) value);
            }
            
            // Delegate to convertToTargetType with an appropriate class
            // This is a hack to handle the internal recursion and type conversion
            try {
                Class<?> effectiveType = targetType;
                return (V) convertToTargetType(value);
            } catch (Exception e) {
                // If we fail, try to create an object from primitive as last resort
                if ((value instanceof Number || value instanceof String || value instanceof Boolean) 
                        && !targetType.isPrimitive() && !Number.class.isAssignableFrom(targetType) 
                        && targetType != String.class && targetType != Boolean.class) {
                    return (V) createObjectFromPrimitive(value, targetType);
                }
                throw e;
            }
        }
    }
    
    /**
     * Helper class for parsing JSON.
     */
    private static class JsonParser {
        private final String json;
        private int pos = 0;
        
        /**
         * Constructs a new JSON parser for the specified JSON string.
         *
         * @param json The JSON string to parse
         */
        public JsonParser(String json) {
            this.json = json;
        }
        
        /**
         * Returns the current position in the JSON string.
         *
         * @return The current position
         */
        public int getPosition() {
            return pos;
        }
        
        /**
         * Returns the character at the current position without advancing the position.
         *
         * @return The current character
         * @throws DeserializationException if the end of the JSON string is reached
         */
        public char peek() {
            if (pos >= json.length()) {
                throw new DeserializationException("Unexpected end of JSON");
            }
            return json.charAt(pos);
        }
        
        /**
         * Returns the character at the current position and advances the position.
         *
         * @return The current character
         * @throws DeserializationException if the end of the JSON string is reached
         */
        public char next() {
            if (pos >= json.length()) {
                throw new DeserializationException("Unexpected end of JSON");
            }
            return json.charAt(pos++);
        }
        
        /**
         * Skips whitespace characters.
         */
        public void skipWhitespace() {
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
                pos++;
            }
        }
        
        /**
         * Expects the specified character at the current position and advances the position.
         *
         * @param c The expected character
         * @throws DeserializationException if the character at the current position does not match the expected character
         */
        public void expect(char c) {
            if (next() != c) {
                throw new DeserializationException("Expected '" + c + "' at position " + (pos - 1));
            }
        }
        
        /**
         * Expects the specified string at the current position and advances the position.
         *
         * @param str The expected string
         * @throws DeserializationException if the string at the current position does not match the expected string
         */
        public void expect(String str) {
            for (int i = 0; i < str.length(); i++) {
                if (next() != str.charAt(i)) {
                    throw new DeserializationException("Expected '" + str + "' at position " + (pos - i - 1));
                }
            }
        }
        
        /**
         * Parses a JSON string at the current position and advances the position.
         *
         * @return The parsed string
         * @throws DeserializationException if the string is not properly formatted
         */
        public String nextString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            boolean escape = false;
            
            while (pos < json.length()) {
                char c = next();
                if (escape) {
                    switch (c) {
                        case '"':
                        case '\\':
                        case '/':
                            sb.append(c);
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'u':
                            if (pos + 4 > json.length()) {
                                throw new DeserializationException("Incomplete Unicode escape sequence");
                            }
                            String hex = json.substring(pos, pos + 4);
                            pos += 4;
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                            } catch (NumberFormatException e) {
                                throw new DeserializationException("Invalid Unicode escape sequence: \\u" + hex);
                            }
                            break;
                        default:
                            sb.append(c);
                    }
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    return sb.toString();
                } else {
                    sb.append(c);
                }
            }
            
            throw new DeserializationException("Unterminated string");
        }
        
        /**
         * Parses a JSON number at the current position and advances the position.
         *
         * @return The parsed number as a Double or Long
         * @throws DeserializationException if the number is not properly formatted
         */
        public Number nextNumber() {
            int start = pos;
            boolean isDouble = false;
            
            // Check for minus sign
            if (peek() == '-') {
                next();
            }
            
            // Parse digits before decimal point
            if (peek() == '0') {
                next();
            } else if (Character.isDigit(peek())) {
                while (pos < json.length() && Character.isDigit(json.charAt(pos))) {
                    pos++;
                }
            } else {
                throw new DeserializationException("Invalid number");
            }
            
            // Parse decimal point and digits after decimal point
            if (pos < json.length() && json.charAt(pos) == '.') {
                isDouble = true;
                pos++;
                if (pos >= json.length() || !Character.isDigit(json.charAt(pos))) {
                    throw new DeserializationException("Invalid number");
                }
                while (pos < json.length() && Character.isDigit(json.charAt(pos))) {
                    pos++;
                }
            }
            
            // Parse exponent
            if (pos < json.length() && (json.charAt(pos) == 'e' || json.charAt(pos) == 'E')) {
                isDouble = true;
                pos++;
                if (pos < json.length() && (json.charAt(pos) == '+' || json.charAt(pos) == '-')) {
                    pos++;
                }
                if (pos >= json.length() || !Character.isDigit(json.charAt(pos))) {
                    throw new DeserializationException("Invalid number");
                }
                while (pos < json.length() && Character.isDigit(json.charAt(pos))) {
                    pos++;
                }
            }
            
            String numberStr = json.substring(start, pos);
            try {
                if (isDouble) {
                    return Double.valueOf(numberStr);
                } else {
                    try {
                        return Long.valueOf(numberStr);
                    } catch (NumberFormatException e) {
                        return Double.valueOf(numberStr);
                    }
                }
            } catch (NumberFormatException e) {
                throw new DeserializationException("Invalid number: " + numberStr);
            }
        }
    }
}