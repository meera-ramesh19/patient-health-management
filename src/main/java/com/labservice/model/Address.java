package com.labservice.model;
import jakarta.persistence.*;
import java.util.List;

@Entity                         // Mark as database table
@Table(name = "addresses")       // Name the table
public class Address {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) // Primary key // Auto-increment
    private Long id;

    @Column(nullable = false)     // Cannot be null in the database
    private String street;

    @Column(nullable = false)     // Cannot be null in the database
    private String city;

    @Column(nullable = false)     // Cannot be null in the database
    private String state;

    @Column(nullable = false)     // Cannot be null in the database
    private String zipCode;

    private Double latitude;  // Optional: for geolocation

    private Double longitude; // Optional: for geolocation


    // Empty constructor (JPA REQUIRES this)
    public Address() {}

    // Getters and Setters for EVERY field
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}