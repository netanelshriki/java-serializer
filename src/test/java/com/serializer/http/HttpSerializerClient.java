package com.serializer.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import com.serializer.api.Deserializer;
import com.serializer.api.Serializer;

/**
 * HTTP client utility that uses the serializer to convert objects to/from JSON
 * for HTTP requests and responses.
 * <p>
 * This class demonstrates how the serialization library can be used with HTTP clients
 * for API communication.
 * </p>
 * 
 * @author java-serializer
 */
public class HttpSerializerClient {
    
    private final CloseableHttpClient httpClient;
    
    /**
     * Constructs a new HTTP client utility.
     */
    public HttpSerializerClient() {
        this.httpClient = HttpClients.createDefault();
    }
    
    /**
     * Sends a GET request to the specified URL and deserializes the response.
     *
     * @param <T> The type of object to deserialize into
     * @param url The URL to send the request to
     * @param deserializer The deserializer to use for the response
     * @return The deserialized response object
     * @throws IOException if an I/O error occurs
     * @throws ParseException if parsing the response fails
     */
    public <T> T get(String url, Deserializer<T> deserializer) throws IOException, ParseException {
        HttpGet request = new HttpGet(url);
        request.setHeader("Accept", "application/json");
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                return deserializer.deserialize(json);
            }
            return null;
        }
    }
    
    /**
     * Sends a POST request to the specified URL with the serialized object as the request body,
     * and deserializes the response.
     *
     * @param <T> The request object type
     * @param <R> The response object type
     * @param url The URL to send the request to
     * @param requestObject The object to serialize and send as the request body
     * @param serializer The serializer to use for the request
     * @param deserializer The deserializer to use for the response
     * @return The deserialized response object
     * @throws IOException if an I/O error occurs
     * @throws ParseException if parsing the response fails
     */
    public <T, R> R post(String url, T requestObject, Serializer<T> serializer, Deserializer<R> deserializer) throws IOException, ParseException {
        HttpPost request = new HttpPost(url);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");
        
        String json = serializer.serialize(requestObject);
        request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String responseJson = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                return deserializer.deserialize(responseJson);
            }
            return null;
        }
    }
    
    /**
     * Closes the HTTP client and releases resources.
     *
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        httpClient.close();
    }
}