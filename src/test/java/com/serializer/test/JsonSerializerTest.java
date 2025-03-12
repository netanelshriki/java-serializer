package com.serializer.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.serializer.Serializers;
import com.serializer.annotation.Expose;
import com.serializer.annotation.JsonIgnore;
import com.serializer.annotation.SerializedName;
import com.serializer.api.Deserializer;
import com.serializer.api.Serializer;
import com.serializer.json.JsonSerializerFactory;

/**
 * Unit tests for JSON serialization and deserialization.
 * 
 * @author java-serializer
 */
public class JsonSerializerTest {
    
    private TestModel testModel;
    private Serializer<TestModel> serializer;
    private Deserializer<TestModel> deserializer;
    
    @BeforeEach
    public void setUp() {
        // Create a test model
        testModel = new TestModel();
        testModel.setId(123);
        testModel.setName("Test Model");
        testModel.setDescription("This is a test model");
        testModel.setCreatedAt(new Date());
        testModel.setEnabled(true);
        testModel.setPrice(99.99);
        testModel.setTags(Arrays.asList("test", "model", "json"));
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("color", "red");
        properties.put("size", "large");
        properties.put("count", 5);
        testModel.setProperties(properties);
        
        // Create serializer and deserializer
        JsonSerializerFactory factory = JsonSerializerFactory.builder()
                .serializeNulls(false)
                .useFieldNames(true)
                .dateFormat("yyyy-MM-dd HH:mm:ss")
                .build();
        
        serializer = factory.getSerializer(TestModel.class);
        deserializer = factory.getDeserializer(TestModel.class);
    }
    
    @Test
    public void testSerialization() {
        String json = serializer.serialize(testModel);
        
        // Basic checks
        assertNotNull(json);
        assertEquals(true, json.contains("\"id\":123"));
        assertEquals(true, json.contains("\"model_name\":\"Test Model\""));
        assertEquals(true, json.contains("\"tags\":"));
        assertEquals(true, json.contains("\"properties\":"));
        
        // Check that password is ignored
        assertEquals(false, json.contains("password"));
    }
    
    @Test
    public void testSerializationWriter() {
        StringWriter writer = new StringWriter();
        serializer.serialize(testModel, writer);
        
        String json = writer.toString();
        assertNotNull(json);
        assertEquals(true, json.contains("\"id\":123"));
    }
    
    @Test
    public void testDeserialization() {
        String json = serializer.serialize(testModel);
        TestModel deserialized = deserializer.deserialize(json);
        
        assertNotNull(deserialized);
        assertEquals(testModel.getId(), deserialized.getId());
        assertEquals(testModel.getName(), deserialized.getName());
        assertEquals(testModel.getDescription(), deserialized.getDescription());
        assertEquals(testModel.isEnabled(), deserialized.isEnabled());
        assertEquals(testModel.getPrice(), deserialized.getPrice(), 0.001);
        assertNotNull(deserialized.getTags());
        assertEquals(testModel.getTags().size(), deserialized.getTags().size());
        assertNotNull(deserialized.getProperties());
        assertEquals(testModel.getProperties().size(), deserialized.getProperties().size());
        
        // Password should be null because it's ignored
        assertNull(deserialized.getPassword());
    }
    
    @Test
    public void testDeserializationReader() {
        String json = serializer.serialize(testModel);
        StringReader reader = new StringReader(json);
        TestModel deserialized = deserializer.deserialize(reader);
        
        assertNotNull(deserialized);
        assertEquals(testModel.getId(), deserialized.getId());
    }
    
    @Test
    public void testFacadeAPIs() {
        // Test toJson
        String json = Serializers.toJson(testModel);
        assertNotNull(json);
        
        // Test fromJson
        TestModel deserialized = Serializers.fromJson(json, TestModel.class);
        assertNotNull(deserialized);
        assertEquals(testModel.getId(), deserialized.getId());
        
        // Test writer/reader
        StringWriter writer = new StringWriter();
        Serializers.toJson(testModel, writer);
        json = writer.toString();
        
        StringReader reader = new StringReader(json);
        deserialized = Serializers.fromJson(reader, TestModel.class);
        assertNotNull(deserialized);
        assertEquals(testModel.getId(), deserialized.getId());
    }
    
    /**
     * Test model class with annotations.
     */
    public static class TestModel {
        private int id;
        
        @SerializedName("model_name")
        private String name;
        
        @Expose(serialize = true, deserialize = true)
        private String description;
        
        @JsonIgnore
        private String password;
        
        private boolean enabled;
        private double price;
        private Date createdAt;
        private List<String> tags;
        private Map<String, Object> properties;
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public double getPrice() {
            return price;
        }
        
        public void setPrice(double price) {
            this.price = price;
        }
        
        public Date getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }
        
        public List<String> getTags() {
            return tags;
        }
        
        public void setTags(List<String> tags) {
            this.tags = tags;
        }
        
        public Map<String, Object> getProperties() {
            return properties;
        }
        
        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }
    }
}