package com.serializer.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.util.Currency;
import java.util.Date;
import java.util.UUID;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.MediaType;

import com.serializer.api.Deserializer;
import com.serializer.api.Serializer;
import com.serializer.api.TypeAdapter;
import com.serializer.exception.DeserializationException;
import com.serializer.exception.SerializationException;
import com.serializer.http.HttpSerializerClient;
import com.serializer.json.JsonSerializerFactory;

/**
 * Test class demonstrating HTTP client/server communication using the serializer
 * with custom type adapters and MockServer.
 * 
 * @author java-serializer
 */
public class CustomAdapterMockServerTest {
    
    private ClientAndServer mockServer;
    private MockServerClient mockServerClient;
    private HttpSerializerClient httpClient;
    private final int PORT = 8081;
    
    @BeforeEach
    public void setup() {
        // Start the mock server
        mockServer = ClientAndServer.startClientAndServer(PORT);
        mockServerClient = new MockServerClient("localhost", PORT);
        httpClient = new HttpSerializerClient();
    }
    
    @AfterEach
    public void tearDown() throws IOException {
        // Clean up resources
        httpClient.close();
        mockServer.stop();
    }
    
    @Test
    public void testProductApiWithCustomAdapters() throws IOException, ParseException {
        // Create a product with custom types
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Smartphone");
        product.setPrice(new Money(799.99, Currency.getInstance("USD")));
        product.setCreatedAt(new Date());
        product.setCategory(ProductCategory.ELECTRONICS);
        product.setInStock(true);
        
        // Create factories with custom type adapters
        JsonSerializerFactory serializerFactory = JsonSerializerFactory.builder()
                .registerTypeAdapter(Money.class, new MoneyTypeAdapter())
                .registerTypeAdapter(Currency.class, new CurrencyTypeAdapter())
                .dateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .prettyPrinting("  ")
                .build();
        
        // Create serializer and deserializer with the custom adapters
        Serializer<Product> serializer = serializerFactory.getSerializer(Product.class);
        Deserializer<Product> deserializer = serializerFactory.getDeserializer(Product.class);
        
        // Serialize the product to JSON
        String productJson = serializer.serialize(product);
        
        // Set up the mock server expectation for GET request
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/products/" + product.getId())
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeaders(
                        new Header("Content-Type", "application/json; charset=utf-8")
                    )
                    .withBody(productJson, MediaType.APPLICATION_JSON)
            );
        
        // Send the GET request and deserialize the response
        Product responseProduct = httpClient.get(
                "http://localhost:" + PORT + "/api/products/" + product.getId(),
                deserializer);
        
        // Verify the response including custom type adapters worked correctly
        assertNotNull(responseProduct);
        assertEquals(product.getId(), responseProduct.getId());
        assertEquals(product.getName(), responseProduct.getName());
        assertNotNull(responseProduct.getPrice());
        assertEquals(product.getPrice().getAmount(), responseProduct.getPrice().getAmount(), 0.001);
        assertEquals(product.getPrice().getCurrency().getCurrencyCode(), 
                    responseProduct.getPrice().getCurrency().getCurrencyCode());
        assertEquals(product.getCategory(), responseProduct.getCategory());
        assertEquals(product.isInStock(), responseProduct.isInStock());
    }
    
    /**
     * Custom type adapter for Currency objects.
     */
    public static class CurrencyTypeAdapter implements TypeAdapter<Currency, String> {
        
        @Override
        public String serialize(Currency value) {
            if (value == null) {
                return null;
            }
            return value.getCurrencyCode();
        }
        
        @Override
        public Currency deserialize(String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }
            try {
                return Currency.getInstance(value);
            } catch (IllegalArgumentException e) {
                throw new DeserializationException("Invalid currency code: " + value, e);
            }
        }
        
        @Override
        public Class<Currency> getType() {
            return Currency.class;
        }
    }
    
    /**
     * Custom type adapter for Money objects.
     */
    public static class MoneyTypeAdapter implements TypeAdapter<Money, String> {
        
        @Override
        public String serialize(Money value) {
            if (value == null) {
                return null;
            }
            return value.getAmount() + " " + value.getCurrency().getCurrencyCode();
        }
        
        @Override
        public Money deserialize(String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }
            try {
                String[] parts = value.split(" ");
                if (parts.length != 2) {
                    throw new DeserializationException("Invalid money format: " + value);
                }
                double amount = Double.parseDouble(parts[0]);
                Currency currency = Currency.getInstance(parts[1]);
                return new Money(amount, currency);
            } catch (Exception e) {
                throw new DeserializationException("Failed to parse money: " + value, e);
            }
        }
        
        @Override
        public Class<Money> getType() {
            return Money.class;
        }
    }
    
    /**
     * Custom Money class that represents an amount with a currency.
     */
    public static class Money {
        private double amount;
        private Currency currency;
        
        public Money() {
        }
        
        public Money(double amount, Currency currency) {
            this.amount = amount;
            this.currency = currency;
        }
        
        public double getAmount() {
            return amount;
        }
        
        public void setAmount(double amount) {
            this.amount = amount;
        }
        
        public Currency getCurrency() {
            return currency;
        }
        
        public void setCurrency(Currency currency) {
            this.currency = currency;
        }
        
        @Override
        public String toString() {
            return amount + " " + currency.getCurrencyCode();
        }
    }
    
    /**
     * Enum representing product categories.
     */
    public enum ProductCategory {
        ELECTRONICS,
        CLOTHING,
        BOOKS,
        HOME,
        SPORTS,
        TOYS
    }
    
    /**
     * Product model class with custom types.
     */
    public static class Product {
        private UUID id;
        private String name;
        private Money price;
        private ProductCategory category;
        private boolean inStock;
        private Date createdAt;
        
        public UUID getId() {
            return id;
        }
        
        public void setId(UUID id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Money getPrice() {
            return price;
        }
        
        public void setPrice(Money price) {
            this.price = price;
        }
        
        public ProductCategory getCategory() {
            return category;
        }
        
        public void setCategory(ProductCategory category) {
            this.category = category;
        }
        
        public boolean isInStock() {
            return inStock;
        }
        
        public void setInStock(boolean inStock) {
            this.inStock = inStock;
        }
        
        public Date getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }
    }
}