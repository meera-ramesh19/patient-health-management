package com.labservice.model;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "pharmacies")
public class Pharmacy {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    private String email;

    @Column(unique = true)
    private String licenseNumber;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    @ElementCollection
    @CollectionTable(name = "pharmacy_hours", joinColumns = @JoinColumn(name = "pharmacy_id"))
    @Column(name = "hours")
    private List<String> hoursOfOperation;  // "Mon: 8AM-9PM", "Tue: 8AM-9PM"

    @ElementCollection
    @CollectionTable(name = "pharmacy_services", joinColumns = @JoinColumn(name = "pharmacy_id"))
    @Column(name = "service")
    private List<String> servicesOffered;   // "Drive-thru", "Delivery", "Immunizations"

    public Pharmacy() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public List<String> getHoursOfOperation() { return hoursOfOperation; }
    public void setHoursOfOperation(List<String> hoursOfOperation) { this.hoursOfOperation = hoursOfOperation; }

    public List<String> getServicesOffered() { return servicesOffered; }
    public void setServicesOffered(List<String> servicesOffered) { this.servicesOffered = servicesOffered; }
}
