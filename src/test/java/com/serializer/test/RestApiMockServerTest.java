package com.serializer.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.MediaType;
import org.mockserver.model.Parameter;
import org.mockserver.verify.VerificationTimes;

import com.serializer.annotation.JsonIgnore;
import com.serializer.annotation.SerializedName;
import com.serializer.api.Deserializer;
import com.serializer.api.Serializer;
import com.serializer.http.HttpSerializerClient;
import com.serializer.json.JsonSerializerFactory;

/**
 * Test class simulating a complete RESTful API with CRUD operations using
 * MockServer and the serialization library.
 * 
 * @author java-serializer
 */
public class RestApiMockServerTest {
    
    private ClientAndServer mockServer;
    private MockServerClient mockServerClient;
    private HttpSerializerClient httpClient;
    private final int PORT = 8082;
    
    // In-memory "database" for the mock server
    private List<Task> taskDatabase;
    private int nextTaskId = 1;
    
    // Serializers and deserializers
    private Serializer<Task> taskSerializer;
    private Deserializer<Task> taskDeserializer;
    private Serializer<CreateTaskRequest> createTaskSerializer;
    private Serializer<UpdateTaskRequest> updateTaskSerializer;
    private Deserializer<Task[]> taskArrayDeserializer;
    
    @BeforeEach
    public void setup() {
        // Initialize the database
        taskDatabase = new ArrayList<>();
        seedDatabase();
        
        // Create serializers and deserializers
        JsonSerializerFactory factory = JsonSerializerFactory.builder()
                .serializeNulls(false)
                .dateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .build();
        
        taskSerializer = factory.getSerializer(Task.class);
        taskDeserializer = factory.getDeserializer(Task.class);
        createTaskSerializer = factory.getSerializer(CreateTaskRequest.class);
        updateTaskSerializer = factory.getSerializer(UpdateTaskRequest.class);
        taskArrayDeserializer = factory.getDeserializer(Task[].class);
        
        // Start the mock server
        mockServer = ClientAndServer.startClientAndServer(PORT);
        mockServerClient = new MockServerClient("localhost", PORT);
        httpClient = new HttpSerializerClient();
        
        // Set up mock API endpoints
        setupMockApiEndpoints();
    }
    
    @AfterEach
    public void tearDown() throws IOException {
        // Clean up resources
        httpClient.close();
        mockServer.stop();
    }
    
    /**
     * Seed the in-memory database with sample tasks.
     */
    private void seedDatabase() {
        Task task1 = new Task();
        task1.setId(nextTaskId++);
        task1.setTitle("Implement serializer");
        task1.setDescription("Create a Java serialization library");
        task1.setStatus(TaskStatus.IN_PROGRESS);
        task1.setCreatedAt(new Date());
        task1.setDueDate(new Date(System.currentTimeMillis() + 86400000)); // Tomorrow
        taskDatabase.add(task1);
        
        Task task2 = new Task();
        task2.setId(nextTaskId++);
        task2.setTitle("Write tests");
        task2.setDescription("Create comprehensive tests for the library");
        task2.setStatus(TaskStatus.TODO);
        task2.setCreatedAt(new Date());
        task2.setDueDate(new Date(System.currentTimeMillis() + 172800000)); // Day after tomorrow
        taskDatabase.add(task2);
    }
    
    /**
     * Set up the mock API endpoints for CRUD operations.
     */
    private void setupMockApiEndpoints() {
        // GET /api/tasks - List all tasks
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/tasks")
            )
            .respond(request -> {
                Task[] tasks = taskDatabase.toArray(new Task[0]);
                String tasksJson = taskSerializer.serialize(tasks);
                return response()
                    .withStatusCode(200)
                    .withHeaders(new Header("Content-Type", "application/json; charset=utf-8"))
                    .withBody(tasksJson, MediaType.APPLICATION_JSON);
            });
        
        // GET /api/tasks/{id} - Get a task by ID
        mockServerClient
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/api/tasks/.*")
            )
            .respond(request -> {
                String path = request.getPath().getValue();
                String idStr = path.substring(path.lastIndexOf('/') + 1);
                int id = Integer.parseInt(idStr);
                
                Task task = findTaskById(id);
                if (task != null) {
                    String taskJson = taskSerializer.serialize(task);
                    return response()
                        .withStatusCode(200)
                        .withHeaders(new Header("Content-Type", "application/json; charset=utf-8"))
                        .withBody(taskJson, MediaType.APPLICATION_JSON);
                } else {
                    return response()
                        .withStatusCode(404)
                        .withHeaders(new Header("Content-Type", "application/json; charset=utf-8"))
                        .withBody("{\"error\":\"Task not found\"}");
                }
            });
        
        // POST /api/tasks - Create a new task
        mockServerClient
            .when(
                request()
                    .withMethod("POST")
                    .withPath("/api/tasks")
                    .withContentType(MediaType.APPLICATION_JSON)
            )
            .respond(request -> {
                String requestBody = request.getBodyAsString();
                // Fix: Use the correct deserializer for CreateTaskRequest
                CreateTaskRequest createRequest = factory.getDeserializer(CreateTaskRequest.class).deserialize(requestBody);
                
                Task newTask = new Task();
                newTask.setId(nextTaskId++);
                newTask.setTitle(createRequest.getTitle());
                newTask.setDescription(createRequest.getDescription());
                newTask.setStatus(TaskStatus.TODO);
                newTask.setCreatedAt(new Date());
                newTask.setDueDate(createRequest.getDueDate());
                
                taskDatabase.add(newTask);
                
                String taskJson = taskSerializer.serialize(newTask);
                return response()
                    .withStatusCode(201)
                    .withHeaders(new Header("Content-Type", "application/json; charset=utf-8"))
                    .withBody(taskJson, MediaType.APPLICATION_JSON);
            });
        
        // PUT /api/tasks/{id} - Update a task
        mockServerClient
            .when(
                request()
                    .withMethod("PUT")
                    .withPath("/api/tasks/.*")
                    .withContentType(MediaType.APPLICATION_JSON)
            )
            .respond(request -> {
                String path = request.getPath().getValue();
                String idStr = path.substring(path.lastIndexOf('/') + 1);
                int id = Integer.parseInt(idStr);
                
                Task existingTask = findTaskById(id);
                if (existingTask != null) {
                    String requestBody = request.getBodyAsString();
                    // Fix: Use the correct deserializer for UpdateTaskRequest
                    UpdateTaskRequest updateRequest = factory.getDeserializer(UpdateTaskRequest.class).deserialize(requestBody);
                    
                    existingTask.setTitle(updateRequest.getTitle());
                    existingTask.setDescription(updateRequest.getDescription());
                    existingTask.setStatus(updateRequest.getStatus());
                    existingTask.setDueDate(updateRequest.getDueDate());
                    
                    String taskJson = taskSerializer.serialize(existingTask);
                    return response()
                        .withStatusCode(200)
                        .withHeaders(new Header("Content-Type", "application/json; charset=utf-8"))
                        .withBody(taskJson, MediaType.APPLICATION_JSON);
                } else {
                    return response()
                        .withStatusCode(404)
                        .withHeaders(new Header("Content-Type", "application/json; charset=utf-8"))
                        .withBody("{\"error\":\"Task not found\"}");
                }
            });
        
        // DELETE /api/tasks/{id} - Delete a task
        mockServerClient
            .when(
                request()
                    .withMethod("DELETE")
                    .withPath("/api/tasks/.*")
            )
            .respond(request -> {
                String path = request.getPath().getValue();
                String idStr = path.substring(path.lastIndexOf('/') + 1);
                int id = Integer.parseInt(idStr);
                
                Task existingTask = findTaskById(id);
                if (existingTask != null) {
                    taskDatabase.remove(existingTask);
                    return response()
                        .withStatusCode(204); // No content
                } else {
                    return response()
                        .withStatusCode(404)
                        .withHeaders(new Header("Content-Type", "application/json; charset=utf-8"))
                        .withBody("{\"error\":\"Task not found\"}");
                }
            });
    }
    
    // Adding the missing JsonSerializerFactory reference for the deserialization in the mock
    private JsonSerializerFactory factory = JsonSerializerFactory.builder()
            .serializeNulls(false)
            .dateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .build();
    
    /**
     * Helper method to find a task by ID in the database.
     *
     * @param id The ID to search for
     * @return The task, or null if not found
     */
    private Task findTaskById(int id) {
        for (Task task : taskDatabase) {
            if (task.getId() == id) {
                return task;
            }
        }
        return null;
    }
    
    @Test
    public void testGetAllTasks() throws IOException, ParseException {
        // Send GET request to fetch all tasks
        Task[] tasks = httpClient.get("http://localhost:" + PORT + "/api/tasks", taskArrayDeserializer);
        
        // Verify the response
        assertNotNull(tasks);
        assertEquals(taskDatabase.size(), tasks.length);
        assertEquals(taskDatabase.get(0).getId(), tasks[0].getId());
        assertEquals(taskDatabase.get(0).getTitle(), tasks[0].getTitle());
        assertEquals(taskDatabase.get(1).getId(), tasks[1].getId());
        assertEquals(taskDatabase.get(1).getTitle(), tasks[1].getTitle());
        
        // Verify the mock server received the expected request
        mockServerClient.verify(
            request()
                .withMethod("GET")
                .withPath("/api/tasks"),
            VerificationTimes.exactly(1)
        );
    }
    
    @Test
    public void testGetTaskById() throws IOException, ParseException {
        // Get an existing task by ID
        Task task = httpClient.get("http://localhost:" + PORT + "/api/tasks/1", taskDeserializer);
        
        // Verify the response
        assertNotNull(task);
        assertEquals(1, task.getId());
        assertEquals("Implement serializer", task.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        
        // Verify the mock server received the expected request
        mockServerClient.verify(
            request()
                .withMethod("GET")
                .withPath("/api/tasks/1"),
            VerificationTimes.exactly(1)
        );
    }
    
    @Test
    public void testCreateTask() throws IOException, ParseException {
        // Create a new task request
        CreateTaskRequest createRequest = new CreateTaskRequest();
        createRequest.setTitle("Write documentation");
        createRequest.setDescription("Create user guide and API documentation");
        createRequest.setDueDate(new Date(System.currentTimeMillis() + 259200000)); // 3 days from now
        
        // Send POST request to create the task
        Task createdTask = httpClient.post(
                "http://localhost:" + PORT + "/api/tasks",
                createRequest,
                createTaskSerializer,
                taskDeserializer);
        
        // Verify the response
        assertNotNull(createdTask);
        assertEquals(3, createdTask.getId()); // Should be the third task
        assertEquals("Write documentation", createdTask.getTitle());
        assertEquals("Create user guide and API documentation", createdTask.getDescription());
        assertEquals(TaskStatus.TODO, createdTask.getStatus()); // Default status
        
        // Verify the task was added to the database
        assertEquals(3, taskDatabase.size());
        
        // Verify the mock server received the expected request
        mockServerClient.verify(
            request()
                .withMethod("POST")
                .withPath("/api/tasks")
                .withContentType(MediaType.APPLICATION_JSON),
            VerificationTimes.exactly(1)
        );
    }
    
    @Test
    public void testUpdateTask() throws IOException, ParseException {
        // Create an update task request
        UpdateTaskRequest updateRequest = new UpdateTaskRequest();
        updateRequest.setTitle("Implement serializer library"); // Changed title
        updateRequest.setDescription("Create a robust Java serialization library"); // Changed description
        updateRequest.setStatus(TaskStatus.DONE); // Changed status
        updateRequest.setDueDate(new Date(System.currentTimeMillis() + 86400000)); // Unchanged
        
        // Send PUT request to update the task
        Task updatedTask = httpClient.post(
                "http://localhost:" + PORT + "/api/tasks/1",
                updateRequest,
                updateTaskSerializer,
                taskDeserializer);
        
        // Verify the response
        assertNotNull(updatedTask);
        assertEquals(1, updatedTask.getId());
        assertEquals("Implement serializer library", updatedTask.getTitle());
        assertEquals("Create a robust Java serialization library", updatedTask.getDescription());
        assertEquals(TaskStatus.DONE, updatedTask.getStatus());
        
        // Verify the mock server received the expected request
        mockServerClient.verify(
            request()
                .withMethod("PUT")
                .withPath("/api/tasks/1"),
            VerificationTimes.exactly(1)
        );
    }
    
    @Test
    public void testDeleteTask() throws IOException {
        // Get initial count
        int initialCount = taskDatabase.size();
        
        // Send DELETE request
        HttpRequest deleteRequest = new HttpRequest()
                .withMethod("DELETE")
                .withPath("/api/tasks/1");
        
        mockServerClient.when(deleteRequest, Times.exactly(1))
                .respond(response().withStatusCode(204));
        
        // Verify the task was removed from the database
        assertEquals(initialCount - 1, taskDatabase.size());
        
        // Verify the mock server received the expected request
        mockServerClient.verify(
            request()
                .withMethod("DELETE")
                .withPath("/api/tasks/1"),
            VerificationTimes.exactly(1)
        );
    }
    
    /**
     * Task status enum.
     */
    public enum TaskStatus {
        TODO,
        IN_PROGRESS,
        DONE
    }
    
    /**
     * Task model class.
     */
    public static class Task {
        private int id;
        private String title;
        private String description;
        private TaskStatus status;
        private Date createdAt;
        private Date dueDate;
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public TaskStatus getStatus() {
            return status;
        }
        
        public void setStatus(TaskStatus status) {
            this.status = status;
        }
        
        public Date getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }
        
        public Date getDueDate() {
            return dueDate;
        }
        
        public void setDueDate(Date dueDate) {
            this.dueDate = dueDate;
        }
    }
    
    /**
     * Request model for creating a task.
     */
    public static class CreateTaskRequest {
        private String title;
        private String description;
        private Date dueDate;
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public Date getDueDate() {
            return dueDate;
        }
        
        public void setDueDate(Date dueDate) {
            this.dueDate = dueDate;
        }
    }
    
    /**
     * Request model for updating a task.
     */
    public static class UpdateTaskRequest {
        private String title;
        private String description;
        private TaskStatus status;
        private Date dueDate;
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public TaskStatus getStatus() {
            return status;
        }
        
        public void setStatus(TaskStatus status) {
            this.status = status;
        }
        
        public Date getDueDate() {
            return dueDate;
        }
        
        public void setDueDate(Date dueDate) {
            this.dueDate = dueDate;
        }
    }
}