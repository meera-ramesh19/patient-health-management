package com.labservice.repository;

import com.labservice.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    // Find all prescriptions from a specific visit
    // Used when viewing visit details: "what was prescribed during this visit?"
    List<Prescription> findByVisitId(Long visitId);

    // Find all prescriptions for a patient (traverses Visit → Patient)
    // Spring reads this as: Prescription.visit.patient.id
    // Used in Patient Portal: "show me all my prescriptions"
    List<Prescription> findByVisitPatientId(Long patientId);

    // Find prescriptions that haven't expired yet
    // Used in Patient Portal: "show me my current medications"
    List<Prescription> findByEndDateAfterOrEndDateIsNull(LocalDate date);

    // Find all prescriptions written by a specific doctor
    List<Prescription> findByPrescribedById(Long doctorId);
}
