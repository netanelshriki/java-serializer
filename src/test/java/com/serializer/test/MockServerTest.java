package com.serializer.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.util.Arrays;
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

import com.serializer.Serializers;
import com.serializer.api.Deserializer;
import com.serializer.api.Serializer;
import com.serializer.http.HttpSerializerClient;

/**
 * Test class demonstrating HTTP client/server communication using the serializer
 * with MockServer.
 * 
 * @author java-serializer
 */
public class MockServerTest {
    
    private ClientAndServer mockServer;
    private MockServerClient mockServerClient;
    private HttpSerializerClient httpClient;
    private final int PORT = 8080;
    
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
    public void testGetRequest() throws IOException, ParseException {
        // Set up test data
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setCreatedAt(new Date());
        
        // Serialize the user to JSON
        String userJson = Serializers.toJson(user);
        
        // Set up the mock server expectation
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/users/1")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeaders(
                        new Header("Content-Type", "application/json; charset=utf-8")
                    )
                    .withBody(userJson, MediaType.APPLICATION_JSON)
            );
        
        // Create a deserializer for the User class
        Deserializer<User> deserializer = Serializers.jsonDeserializer(User.class);
        
        // Send the GET request and deserialize the response
        User responseUser = httpClient.get("http://localhost:" + PORT + "/api/users/1", deserializer);
        
        // Verify the response
        assertNotNull(responseUser);
        assertEquals(user.getId(), responseUser.getId());
        assertEquals(user.getUsername(), responseUser.getUsername());
        assertEquals(user.getEmail(), responseUser.getEmail());
    }
    
    @Test
    public void testPostRequest() throws IOException, ParseException {
        // Set up test data for request and response
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setUsername("newuser");
        createRequest.setEmail("newuser@example.com");
        createRequest.setPassword("password123");
        
        User createdUser = new User();
        createdUser.setId(UUID.randomUUID());
        createdUser.setUsername(createRequest.getUsername());
        createdUser.setEmail(createRequest.getEmail());
        createdUser.setCreatedAt(new Date());
        
        // Serialize the created user to JSON for the mock response
        String createdUserJson = Serializers.toJson(createdUser);
        
        // Set up the mock server expectation
        mockServerClient
            .when(
                request()
                    .withMethod("POST")
                    .withPath("/api/users")
                    .withContentType(MediaType.APPLICATION_JSON)
            )
            .respond(
                response()
                    .withStatusCode(201)
                    .withHeaders(
                        new Header("Content-Type", "application/json; charset=utf-8")
                    )
                    .withBody(createdUserJson, MediaType.APPLICATION_JSON)
            );
        
        // Create serializer and deserializer
        Serializer<CreateUserRequest> requestSerializer = Serializers.jsonSerializer(CreateUserRequest.class);
        Deserializer<User> responseDeserializer = Serializers.jsonDeserializer(User.class);
        
        // Send the POST request with serialized object and deserialize the response
        User responseUser = httpClient.post(
                "http://localhost:" + PORT + "/api/users",
                createRequest,
                requestSerializer,
                responseDeserializer
        );
        
        // Verify the response
        assertNotNull(responseUser);
        assertEquals(createdUser.getId(), responseUser.getId());
        assertEquals(createdUser.getUsername(), responseUser.getUsername());
        assertEquals(createdUser.getEmail(), responseUser.getEmail());
    }
    
    @Test
    public void testGetListOfObjects() throws IOException, ParseException {
        // Set up test data - a list of users
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        
        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        
        User[] users = new User[] { user1, user2 };
        
        // Serialize the array of users to JSON
        String usersJson = Serializers.toJson(users);
        
        // Set up the mock server expectation
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/users")
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeaders(
                        new Header("Content-Type", "application/json; charset=utf-8")
                    )
                    .withBody(usersJson, MediaType.APPLICATION_JSON)
            );
        
        // Create a deserializer for the User array class
        Deserializer<User[]> deserializer = Serializers.jsonDeserializer(User[].class);
        
        // Send the GET request and deserialize the response
        User[] responseUsers = httpClient.get("http://localhost:" + PORT + "/api/users", deserializer);
        
        // Verify the response
        assertNotNull(responseUsers);
        assertEquals(2, responseUsers.length);
        assertEquals(user1.getId(), responseUsers[0].getId());
        assertEquals(user1.getUsername(), responseUsers[0].getUsername());
        assertEquals(user2.getId(), responseUsers[1].getId());
        assertEquals(user2.getUsername(), responseUsers[1].getUsername());
    }
    
    /**
     * Example model class for a user.
     */
    public static class User {
        private UUID id;
        private String username;
        private String email;
        private Date createdAt;
        
        public UUID getId() {
            return id;
        }
        
        public void setId(UUID id) {
            this.id = id;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public Date getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }
    }
    
    /**
     * Example request model for creating a user.
     */
    public static class CreateUserRequest {
        private String username;
        private String email;
        private String password;
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
}