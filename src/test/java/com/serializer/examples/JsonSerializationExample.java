package com.serializer.examples;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.serializer.Serializers;
import com.serializer.annotation.Expose;
import com.serializer.annotation.JsonIgnore;
import com.serializer.annotation.SerializedName;
import com.serializer.api.Serializer;
import com.serializer.json.JsonSerializerFactory;

/**
 * Example class demonstrating the use of the serialization library for JSON serialization.
 * 
 * @author java-serializer
 */
public class JsonSerializationExample {
    
    /**
     * Main method demonstrating the library usage.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Create a sample person object
        Person person = new Person();
        person.setId(UUID.randomUUID());
        person.setFirstName("John");
        person.setLastName("Doe");
        person.setEmail("john.doe@example.com");
        person.setCreatedAt(new Date());
        person.setUpdatedAt(new Date());
        person.setAge(30);
        person.setHeight(1.85);
        person.setWeight(75.5);
        
        // Add some addresses
        Address homeAddress = new Address();
        homeAddress.setType("home");
        homeAddress.setStreet("123 Main St");
        homeAddress.setCity("Anytown");
        homeAddress.setState("CA");
        homeAddress.setZip("12345");
        homeAddress.setCountry("USA");
        
        Address workAddress = new Address();
        workAddress.setType("work");
        workAddress.setStreet("456 Market St");
        workAddress.setCity("Anytown");
        workAddress.setState("CA");
        workAddress.setZip("12345");
        workAddress.setCountry("USA");
        
        person.setAddresses(Arrays.asList(homeAddress, workAddress));
        
        // Default serialization
        System.out.println("===== Default Serialization =====");
        String json = Serializers.toJson(person);
        System.out.println(json);
        
        // Custom serialization with builder
        System.out.println("\n===== Custom Serialization =====");
        Serializer<Person> serializer = JsonSerializerFactory.builder()
                .serializeNulls(false)
                .useFieldNames(true)
                .dateFormat("yyyy-MM-dd HH:mm:ss")
                .prettyPrinting("    ")
                .build()
                .getSerializer(Person.class);
        
        json = serializer.serialize(person);
        System.out.println(json);
        
        // Deserialization
        System.out.println("\n===== Deserialization =====");
        Person deserializedPerson = Serializers.fromJson(json, Person.class);
        System.out.println("Deserialized person: " + deserializedPerson);
    }
    
    /**
     * Example model class with various annotations.
     */
    public static class Person {
        @SerializedName("person_id")
        private UUID id;
        
        @SerializedName("first_name")
        private String firstName;
        
        @SerializedName("last_name")
        private String lastName;
        
        @Expose(serialize = true, deserialize = true)
        private String email;
        
        @JsonIgnore
        private String password;
        
        private int age;
        private double height;
        private double weight;
        
        @SerializedName("created")
        private Date createdAt;
        
        @SerializedName("updated")
        private Date updatedAt;
        
        private List<Address> addresses;
        
        public UUID getId() {
            return id;
        }
        
        public void setId(UUID id) {
            this.id = id;
        }
        
        public String getFirstName() {
            return firstName;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
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
        
        public int getAge() {
            return age;
        }
        
        public void setAge(int age) {
            this.age = age;
        }
        
        public double getHeight() {
            return height;
        }
        
        public void setHeight(double height) {
            this.height = height;
        }
        
        public double getWeight() {
            return weight;
        }
        
        public void setWeight(double weight) {
            this.weight = weight;
        }
        
        public Date getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }
        
        public Date getUpdatedAt() {
            return updatedAt;
        }
        
        public void setUpdatedAt(Date updatedAt) {
            this.updatedAt = updatedAt;
        }
        
        public List<Address> getAddresses() {
            return addresses;
        }
        
        public void setAddresses(List<Address> addresses) {
            this.addresses = addresses;
        }
        
        @Override
        public String toString() {
            return "Person [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email
                    + ", age=" + age + ", addresses=" + addresses + "]";
        }
    }
    
    /**
     * Example model class for addresses.
     */
    public static class Address {
        private String type;
        private String street;
        private String city;
        private String state;
        private String zip;
        private String country;
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getStreet() {
            return street;
        }
        
        public void setStreet(String street) {
            this.street = street;
        }
        
        public String getCity() {
            return city;
        }
        
        public void setCity(String city) {
            this.city = city;
        }
        
        public String getState() {
            return state;
        }
        
        public void setState(String state) {
            this.state = state;
        }
        
        public String getZip() {
            return zip;
        }
        
        public void setZip(String zip) {
            this.zip = zip;
        }
        
        public String getCountry() {
            return country;
        }
        
        public void setCountry(String country) {
            this.country = country;
        }
        
        @Override
        public String toString() {
            return "Address [type=" + type + ", street=" + street + ", city=" + city + ", state=" + state + ", zip=" + zip
                    + ", country=" + country + "]";
        }
    }
}