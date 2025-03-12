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

This library implements several design patterns:

- **Builder Pattern**: For creating serializers and deserializers with fluent configuration
- **Factory Pattern**: For creating serializer and deserializer instances
- **Adapter Pattern**: For adapting types during serialization and deserialization
- **Strategy Pattern**: For different serialization strategies
- **Facade Pattern**: Provides a simplified interface to the library
- **Decorator Pattern**: For adding functionality to serializers and deserializers

## License

This library is released under the MIT License.
