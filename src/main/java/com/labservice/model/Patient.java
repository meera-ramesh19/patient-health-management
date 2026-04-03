package com.labservice.model;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity                         // Mark as database table
@Table(name = "patients")        // Name the table
public class Patient {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) // Primary key // Auto-increment
    private Long id;

    @Column(nullable = false)     // Cannot be null in the database
    private String firstName;

    @Column(nullable = false)     // Cannot be null in the database
    private String lastName;

    @Column(nullable = false) // Cannot be null and must be unique
    private LocalDate dateOfBirth;

    @Column(nullable = false, unique=true)  // Cannot be null and must be unique
    private String email;

    @OneToOne(cascade = CascadeType.ALL)    // If patient is saved, save address too
    @JoinColumn(name = "address_id")         // Creates a foreign key column
    private Address address;

    @Column(nullable = false)
    private String phoneNumber;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor primaryDoctor;

    @ManyToOne
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy preferredPharmacy;     // Where prescriptions get sent

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;                      // Links to login account

    @OneToMany(mappedBy = "patient")
    private List<Visit> visits;

    @OneToMany(mappedBy = "patient")
    private List<Appointment> appointments;
    @ElementCollection
    @CollectionTable(name = "patient_conditions", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "condition") 
    private List<String> medicalConditions;  // E.g. "Diabetes", "Hypertension"

    @ElementCollection
    @CollectionTable(name = "patient_allergies", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "allergy")
    private List<String> allergies;          // E.g. "Penicillin", "Peanuts

    @ElementCollection
    @CollectionTable(name = "patient_medications", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "medication")
    private List<String> medications;        // E.g. "Metformin", "Lisin

    @ElementCollection
    @CollectionTable(name = "patient_emergency_contacts", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "emergency_contact")
    private List<String> emergencyContacts;  // E.g. "John Doe: 555

    @ElementCollection
    @CollectionTable(name = "patient_insurance", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "insurance_provider")
    private List<String> insuranceProviders; // E.g. "Blue Cross", "Aetna

    //@ElementCollection
    //@CollectionTable(name = "patient_preferred_doctors", joinColumns = @JoinColumn(name = "patient_id"))
    //@Column(name = "preferred_doctor")
    //private List<String> preferredDoctors;   // E.g. "Dr. Smith", "Dr. Johnson"

    public Patient() {}
    
    // Getters and Setters for EVERY field
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }   

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; } 

    public List<String> getMedicalConditions() { return medicalConditions; }
    public void setMedicalConditions(List<String> medicalConditions) { this.medicalConditions = medicalConditions; }   

    public List<String> getAllergies() { return allergies; }
    public void setAllergies(List<String> allergies) { this.allergies = allergies; }

    public List<String> getMedications() { return medications; }
    public void setMedications(List<String> medications) { this.medications = medications; }

    public List<String> getEmergencyContacts() { return emergencyContacts; }
    public void setEmergencyContacts(List<String> emergencyContacts) { this.emergencyContacts = emergencyContacts; }

    public List<String> getInsuranceProviders() { return insuranceProviders; }
    public void setInsuranceProviders(List<String> insuranceProviders) { this.insuranceProviders = insuranceProviders; }

    //public List<String> getPreferredDoctors() { return preferredDoctors; }
    //public void setPreferredDoctors(List<String> preferredDoctors) { this.preferredDoctors = preferredDoctors; }    
    
    public Doctor getPrimaryDoctor() { return primaryDoctor; }
    public void setPrimaryDoctor(Doctor primaryDoctor) { this.primaryDoctor = primaryDoctor; }

    public Pharmacy getPreferredPharmacy() { return preferredPharmacy; }
    public void setPreferredPharmacy(Pharmacy preferredPharmacy) { this.preferredPharmacy = preferredPharmacy; }

    public List<Visit> getVisits() { return visits; }
    public void setVisits(List<Visit> visits) { this.visits = visits; }

    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }
}