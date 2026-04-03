package com.labservice.model;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "lab_orders")
public class LabOrder {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String testName;            // "Complete Blood Count", "Lipid Panel"

    private String testCode;            // "CBC", "LP"

    @Column(nullable = false)
    private String status;              // "ORDERED", "IN_PROGRESS", "COMPLETED", "CANCELLED"

    private LocalDate orderedDate;
    private LocalDate completedDate;
    private String results;             // Lab results text (filled when COMPLETED)
    private String notes;

    @ManyToOne
    @JoinColumn(name = "visit_id")
    private Visit visit;                // Which visit this was ordered during (optional)

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;            // Which patient this lab is for

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor orderedBy;           // Which doctor ordered the lab

    public LabOrder() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getTestCode() { return testCode; }
    public void setTestCode(String testCode) { this.testCode = testCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getOrderedDate() { return orderedDate; }
    public void setOrderedDate(LocalDate orderedDate) { this.orderedDate = orderedDate; }

    public LocalDate getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDate completedDate) { this.completedDate = completedDate; }

    public String getResults() { return results; }
    public void setResults(String results) { this.results = results; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Visit getVisit() { return visit; }
    public void setVisit(Visit visit) { this.visit = visit; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getOrderedBy() { return orderedBy; }
    public void setOrderedBy(Doctor orderedBy) { this.orderedBy = orderedBy; }
}
