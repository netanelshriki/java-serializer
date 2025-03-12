# Java Serializer

A lightweight, flexible, and enterprise-grade Java serialization and deserialization library with a fluent API. This library provides serialization capabilities without external dependencies like Jackson or Gson.

## Features

- JSON serialization and deserialization
- Type-safe serialization and deserialization
- Rich annotation support for customizing serialization behavior
- Pre-built type adapters for common Java types
- Extensible architecture for supporting custom types
- Multiple configuration options via a fluent builder API
- Pretty printing support
- Zero external dependencies (except for testing)

## Getting Started

### Maven Dependency

Include the library in your project using Maven:

```xml
<dependency>
    <groupId>com.serializer</groupId>
    <artifactId>java-serializer</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

Serialize an object to JSON:

```java
// Create a person object
Person person = new Person();
person.setId(1);
person.setName("John Doe");
person.setEmail("john.doe@example.com");

// Serialize to JSON
String json = Serializers.toJson(person);
```

Deserialize JSON to an object:

```java
// Deserialize from JSON
Person person = Serializers.fromJson(json, Person.class);
```

### Custom Configuration

Configure serialization with the builder API:

```java
// Create a custom serializer
Serializer<Person> serializer = JsonSerializerFactory.builder()
        .serializeNulls(false)          // Skip null fields
        .useFieldNames(true)            // Use field names as-is
        .dateFormat("yyyy-MM-dd")       // Custom date format
        .prettyPrinting("  ")           // Enable pretty printing
        .build()
        .getSerializer(Person.class);

// Serialize using the custom serializer
String json = serializer.serialize(person);
```

## Annotations

The library provides several annotations to customize serialization behavior:

### @SerializedName

Specifies a custom name for a field during serialization:

```java
public class Person {
    @SerializedName("user_id")
    private long id;
    
    @SerializedName("full_name")
    private String name;
    
    // ...
}
```

### @Expose

Controls whether a field should be included in serialization and/or deserialization:

```java
public class Person {
    @Expose(serialize = true, deserialize = false)
    private String readOnlyField;
    
    @Expose(serialize = false, deserialize = true)
    private String writeOnlyField;
    
    // ...
}
```

### @JsonIgnore

Excludes a field from both serialization and deserialization:

```java
public class User {
    private String username;
    
    @JsonIgnore
    private String password;
    
    // ...
}
```

### @DateFormat

Specifies a custom date format for a date field:

```java
public class Event {
    @DateFormat("yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    
    @DateFormat("yyyy-MM-dd")
    private Date date;
    
    // ...
}
```

### @TypeAdapter

Specifies a custom type adapter for a field:

```java
public class Person {
    @TypeAdapter(CustomDateAdapter.class)
    private Date birthDate;
    
    // ...
}
```

## Type Adapters

Type adapters provide custom serialization and deserialization logic for specific types. The library includes built-in adapters for common types:

- `DateTypeAdapter` - for `java.util.Date`
- `EnumTypeAdapter` - for enum types
- `LocalDateTimeTypeAdapter` - for `java.time.LocalDateTime`
- `UUIDTypeAdapter` - for `java.util.UUID`
- `URLTypeAdapter` - for `java.net.URL`

You can create custom type adapters by implementing the `TypeAdapter` interface:

```java
public class CustomDateAdapter implements TypeAdapter<Date, String> {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    @Override
    public String serialize(Date value) {
        if (value == null) {
            return null;
        }
        return dateFormat.format(value);
    }
    
    @Override
    public Date deserialize(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return dateFormat.parse(value);
        } catch (ParseException e) {
            throw new DeserializationException("Invalid date format: " + value, e);
        }
    }
    
    @Override
    public Class<Date> getType() {
        return Date.class;
    }
}
```

Register a custom type adapter:

```java
JsonSerializerFactory factory = JsonSerializerFactory.builder()
        .registerTypeAdapter(Date.class, new CustomDateAdapter())
        .build();
```

## Advanced Usage

### Streaming Serialization

Serialize directly to a writer or output stream:

```java
// Serialize to a writer
Writer writer = new FileWriter("person.json");
Serializers.toJson(person, writer);
writer.close();

// Serialize to an output stream
OutputStream outputStream = new FileOutputStream("person.json");
Serializers.toJson(person, outputStream);
outputStream.close();
```

### Streaming Deserialization

Deserialize directly from a reader or input stream:

```java
// Deserialize from a reader
Reader reader = new FileReader("person.json");
Person person = Serializers.fromJson(reader, Person.class);
reader.close();

// Deserialize from an input stream
InputStream inputStream = new FileInputStream("person.json");
Person person = Serializers.fromJson(inputStream, Person.class);
inputStream.close();
```

## HTTP Integration

The library can be easily integrated with HTTP clients for API communication. Here's an example using the Apache HttpClient:

```java
// Create a serializer and deserializer
Serializer<User> serializer = Serializers.jsonSerializer(User.class);
Deserializer<User> deserializer = Serializers.jsonDeserializer(User.class);

// Create an HTTP client
HttpSerializerClient httpClient = new HttpSerializerClient();

// Send a POST request with serialized object
User user = new User("john.doe", "password123");
User createdUser = httpClient.post("https://api.example.com/users", user, serializer, deserializer);

// Send a GET request and deserialize the response
User fetchedUser = httpClient.get("https://api.example.com/users/123", deserializer);
```

See the `src/test/java/com/serializer/test/MockServerTest.java` for complete examples of HTTP integration with MockServer.

## Custom Type Adapters with HTTP

You can use custom type adapters with HTTP communication for handling complex types:

```java
// Create a serializer factory with custom type adapters
JsonSerializerFactory factory = JsonSerializerFactory.builder()
        .registerTypeAdapter(Money.class, new MoneyTypeAdapter())
        .registerTypeAdapter(Currency.class, new CurrencyTypeAdapter())
        .build();

// Create serializers and deserializers
Serializer<Product> serializer = factory.getSerializer(Product.class);
Deserializer<Product> deserializer = factory.getDeserializer(Product.class);

// Use with HTTP client
Product product = httpClient.get("https://api.example.com/products/123", deserializer);
```

See `src/test/java/com/serializer/test/CustomAdapterMockServerTest.java` for a complete example of custom type adapters with HTTP.

## RESTful API Example

The library can be used to build complete RESTful APIs. See `src/test/java/com/serializer/test/RestApiMockServerTest.java` for an example that implements a full CRUD API with proper error handling.

## Design Patterns

This library implements several design patterns with concrete examples from the source code:

### Builder Pattern

The Builder Pattern is used extensively to provide a fluent API for configuration. For example, in `JsonSerializerFactory.Builder`:

```java
// From JsonSerializerFactory.java
public static class Builder {
    private final com.serializer.impl.DefaultConfig config;
    
    private Builder() {
        this.config = new com.serializer.impl.DefaultConfig(new DefaultSerializerFactory());
    }
    
    public Builder serializeNulls(boolean serializeNulls) {
        config.serializeNulls(serializeNulls);
        return this;
    }
    
    public Builder useFieldNames(boolean useFieldNames) {
        config.useFieldNames(useFieldNames);
        return this;
    }
    
    public Builder dateFormat(String dateFormat) {
        config.dateFormat(dateFormat);
        return this;
    }
    
    // More builder methods...
    
    public JsonSerializerFactory build() {
        SerializationContext context = config.createContext();
        JsonSerializerFactory factory = new JsonSerializerFactory(context);
        return factory;
    }
}
```

### Factory Pattern

The Factory Pattern is used to create serializer and deserializer instances. For example, in `DefaultSerializerFactory`:

```java
// From DefaultSerializerFactory.java
@Override
@SuppressWarnings("unchecked")
public <T> Serializer<T> getSerializer(Class<T> type) {
    Serializer<T> serializer = (Serializer<T>) serializers.get(type);
    if (serializer == null) {
        serializer = createSerializer(type);
        serializers.put(type, serializer);
    }
    return serializer;
}

@Override
@SuppressWarnings("unchecked")
public <T> Deserializer<T> getDeserializer(Class<T> type) {
    Deserializer<T> deserializer = (Deserializer<T>) deserializers.get(type);
    if (deserializer == null) {
        deserializer = createDeserializer(type);
        deserializers.put(type, deserializer);
    }
    return deserializer;
}
```

### Adapter Pattern

The Adapter Pattern is used for type conversion. For example, in `DateTypeAdapter`:

```java
// From DateTypeAdapter.java
public class DateTypeAdapter implements TypeAdapter<Date, String> {
    private final SimpleDateFormat dateFormat;
    
    public DateTypeAdapter(String dateFormatPattern) {
        this.dateFormat = new SimpleDateFormat(dateFormatPattern);
    }
    
    @Override
    public String serialize(Date value) {
        if (value == null) {
            return null;
        }
        try {
            synchronized (dateFormat) {
                return dateFormat.format(value);
            }
        } catch (Exception e) {
            throw new SerializationException("Failed to format date: " + value, e);
        }
    }
    
    @Override
    public Date deserialize(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            synchronized (dateFormat) {
                return dateFormat.parse(value);
            }
        } catch (ParseException e) {
            throw new DeserializationException("Failed to parse date: " + value, e);
        }
    }
}
```

### Strategy Pattern

The Strategy Pattern is used to implement different serialization strategies. For example, through the `SerializeStrategy` annotation:

```java
// From SerializeStrategy.java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SerializeStrategy {
    
    /**
     * The serialization strategy class to use for the annotated type.
     */
    Class<?> value();
}

// Implementation in JsonSerializer for different serialization strategies
private void serializeObject(Object object, JsonWriter jsonWriter) throws Exception {
    // Apply different strategies based on object type
    if (object instanceof Map) {
        serializeMap((Map<?, ?>) object, jsonWriter);
    } else if (object instanceof Collection) {
        serializeCollection((Collection<?>) object, jsonWriter);
    } else if (objectType.isArray()) {
        serializeArray(object, jsonWriter);
    } else {
        // Complex object strategy
        // ...
    }
}
```

### Facade Pattern

The Facade Pattern provides a simplified interface through the `Serializers` class:

```java
// From Serializers.java
public final class Serializers {
    
    private Serializers() {
        // Private constructor to prevent instantiation
    }
    
    public static <T> Serializer<T> jsonSerializer(Class<T> type) {
        return createDefaultJsonFactory().getSerializer(type);
    }
    
    public static <T> Deserializer<T> jsonDeserializer(Class<T> type) {
        return createDefaultJsonFactory().getDeserializer(type);
    }
    
    public static <T> String toJson(T object) {
        if (object == null) {
            return "null";
        }
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) object.getClass();
        return jsonSerializer(type).serialize(object);
    }
    
    public static <T> T fromJson(String json, Class<T> type) {
        return jsonDeserializer(type).deserialize(json);
    }
    
    // More utility methods...
}
```

### Decorator Pattern

The Decorator Pattern is used to add features to serializers. For example, in `JsonWriter`:

```java
// From JsonSerializer.java - JsonWriter inner class
private static class JsonWriter {
    private final Writer writer;
    private final StringBuilder stringBuilder;
    private final String indentation;
    private int indentLevel = 0;
    
    // Constructors...
    
    public void writeString(String string) throws IOException {
        write('"');
        write(StringUtils.escapeJson(string));
        write('"');
    }
    
    public void beginObject() throws IOException {
        write('{');
        indentLevel++;
        writeNewLineAndIndent();
    }
    
    public void endObject() throws IOException {
        indentLevel--;
        writeNewLineAndIndent();
        write('}');
    }
    
    // The decorator adds pretty printing functionality
    private void writeNewLineAndIndent() throws IOException {
        if (isPrettyPrinting()) {
            write('\n');
            for (int i = 0; i < indentLevel; i++) {
                write(indentation);
            }
        }
    }
}
```

## License

This library is released under the MIT License.
