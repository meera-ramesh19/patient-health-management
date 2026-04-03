package com.labservice.service;

import com.labservice.exception.BadRequestException;
import com.labservice.exception.ResourceNotFoundException;
import com.labservice.model.Doctor;
import com.labservice.model.Prescription;
import com.labservice.model.Visit;
import com.labservice.repository.DoctorRepository;
import com.labservice.repository.PrescriptionRepository;
import com.labservice.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    // Get all prescriptions (Admin Portal)
    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAll();
    }

    // Get one prescription by ID
    public Prescription getPrescriptionById(Long id) {
        return prescriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + id));
    }

    // Get all prescriptions from a specific visit
    public List<Prescription> getPrescriptionsByVisit(Long visitId) {
        return prescriptionRepository.findByVisitId(visitId);
    }

    // Get all prescriptions for a patient (Patient Portal: "my medications")
    // Traverses: Prescription → Visit → Patient
    public List<Prescription> getPrescriptionsByPatient(Long patientId) {
        return prescriptionRepository.findByVisitPatientId(patientId);
    }

    // Get active prescriptions (not expired)
    public List<Prescription> getActivePrescriptions() {
        return prescriptionRepository.findByEndDateAfterOrEndDateIsNull(LocalDate.now());
    }

    // Get prescriptions written by a specific doctor (Doctor Portal)
    public List<Prescription> getPrescriptionsByDoctor(Long doctorId) {
        return prescriptionRepository.findByPrescribedById(doctorId);
    }

    // Write a new prescription (Doctor Portal)
    public Prescription createPrescription(Long visitId, Long doctorId,
                                            String medicationName, String dosage,
                                            String frequency, LocalDate startDate,
                                            LocalDate endDate, String instructions) {

        // Verify visit exists
        Visit visit = visitRepository.findById(visitId)
            .orElseThrow(() -> new ResourceNotFoundException("Visit not found"));

        // Verify doctor exists
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        // Business rule: end date must be after start date
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date");
        }

        Prescription prescription = new Prescription();
        prescription.setVisit(visit);
        prescription.setPrescribedBy(doctor);
        prescription.setMedicationName(medicationName);
        prescription.setDosage(dosage);
        prescription.setFrequency(frequency);
        prescription.setStartDate(startDate);
        prescription.setEndDate(endDate);
        prescription.setInstructions(instructions);

        return prescriptionRepository.save(prescription);
    }

    // Update a prescription (Doctor Portal)
    public Prescription updatePrescription(Long id, String dosage, String frequency,
                                            LocalDate endDate, String instructions) {
        Prescription prescription = getPrescriptionById(id);
        prescription.setDosage(dosage);
        prescription.setFrequency(frequency);
        prescription.setEndDate(endDate);
        prescription.setInstructions(instructions);
        return prescriptionRepository.save(prescription);
    }

    // Delete a prescription
    public void deletePrescription(Long id) {
        prescriptionRepository.deleteById(id);
    }
}
