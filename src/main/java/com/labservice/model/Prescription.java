package com.labservice.model;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String medicationName;      // "Amoxicillin", "Ibuprofen"

    private String dosage;              // "500mg"
    private String frequency;           // "Twice daily", "Every 8 hours"
    private LocalDate startDate;
    private LocalDate endDate;
    private String instructions;        // "Take with food", "Do not drive"

    @ManyToOne
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;                // Which visit this was prescribed during

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor prescribedBy;        // Which doctor wrote the prescription

    @ManyToOne
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy;          // Where this prescription is sent to be filled

    public Prescription() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Visit getVisit() { return visit; }
    public void setVisit(Visit visit) { this.visit = visit; }

    public Doctor getPrescribedBy() { return prescribedBy; }
    public void setPrescribedBy(Doctor prescribedBy) { this.prescribedBy = prescribedBy; }

    public Pharmacy getPharmacy() { return pharmacy; }
    public void setPharmacy(Pharmacy pharmacy) { this.pharmacy = pharmacy; }
}
