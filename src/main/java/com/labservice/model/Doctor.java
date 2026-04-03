package com.labservice.model;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name="doctors")
public class Doctor {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique=true)
    private String email;
    @OneToOne(cascade = CascadeType.ALL)    // If doctor is saved, save address too
    @JoinColumn(name = "address_id")         // Creates a foreign key column
    private Address address;

    @Column(nullable = false)
    private String specialization;
    @Column(nullable = false)
    private String phoneNumber;
    @Column(nullable = false)
    private String hospitalAffiliation;
    @Column(nullable = false)
    private Integer yearsOfExperience;
    @Column(nullable = false)
    private Double consultationFee;


    @ElementCollection
    @CollectionTable(name = "doctor_insurance", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "insurance_provider")
    private List<String> insuranceAccepted;

    @ElementCollection
    @CollectionTable(name = "doctor_hours", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "available_hour")
    private List<String> availableHours;

    @ElementCollection
    @CollectionTable(name = "doctor_days", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "available_day")
    private List<String> daysAvailable;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;                      // Links to login account

    @OneToMany(mappedBy = "doctor")
    private List<Visit> visits;             // Visits this doctor conducted

    @OneToMany(mappedBy = "doctor")
    private List<Appointment> appointments;

    public Doctor() {}
    // Getters and Setters for EVERY field
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
     
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getHospitalAffiliation() { return hospitalAffiliation; }
    public void setHospitalAffiliation(String hospitalAffiliation) { this.hospitalAffiliation = hospitalAffiliation; }

    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public Double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(Double consultationFee) { this.consultationFee = consultationFee; }

    public List<String> getInsuranceAccepted() { return insuranceAccepted; }
    public void setInsuranceAccepted(List<String> insuranceAccepted) { this.insuranceAccepted = insuranceAccepted; }

    public List<String> getAvailableHours() { return availableHours; }
    public void setAvailableHours(List<String> availableHours) { this.availableHours = availableHours; }

    public List<String> getDaysAvailable() { return daysAvailable; }
    public void setDaysAvailable(List<String> daysAvailable) { this.daysAvailable = daysAvailable; }    

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Visit> getVisits() { return visits; }
    public void setVisits(List<Visit> visits) { this.visits = visits; }

    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }
}