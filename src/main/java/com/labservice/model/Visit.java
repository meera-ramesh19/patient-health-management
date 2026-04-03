package com.labservice.model;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "visits")
public class Visit {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDate visitDate;

    @Column(nullable = false)
    private String reason;

    private String diagnosis;
    private String notes;
    private String visitType;       // "IN_PERSON", "TELEHEALTH", "FOLLOW_UP", "EMERGENCY"

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL)
    private List<Prescription> prescriptions;

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL)
    private List<LabOrder> labOrders;

    public Visit() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getVisitType() { return visitType; }
    public void setVisitType(String visitType) { this.visitType = visitType; }

    public List<Prescription> getPrescriptions() { return prescriptions; }
    public void setPrescriptions(List<Prescription> prescriptions) { this.prescriptions = prescriptions; }

    public List<LabOrder> getLabOrders() { return labOrders; }
    public void setLabOrders(List<LabOrder> labOrders) { this.labOrders = labOrders; }
}
