package com.labservice.service;

import com.labservice.exception.ResourceNotFoundException;
import com.labservice.model.Doctor;
import com.labservice.model.Patient;
import com.labservice.model.Visit;
import com.labservice.repository.DoctorRepository;
import com.labservice.repository.PatientRepository;
import com.labservice.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class VisitService {

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    // Get all visits (Admin Portal)
    public List<Visit> getAllVisits() {
        return visitRepository.findAll();
    }

    // Get one visit by ID
    public Visit getVisitById(Long id) {
        return visitRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Visit not found with id: " + id));
    }

    // Get all visits for a patient (Patient Portal: "my visit history")
    public List<Visit> getVisitsByPatient(Long patientId) {
        return visitRepository.findByPatientId(patientId);
    }

    // Get visits for a patient, newest first
    public List<Visit> getVisitsByPatientNewestFirst(Long patientId) {
        return visitRepository.findByPatientIdOrderByVisitDateDesc(patientId);
    }

    // Get all visits by a doctor (Doctor Portal: "visits I conducted")
    public List<Visit> getVisitsByDoctor(Long doctorId) {
        return visitRepository.findByDoctorId(doctorId);
    }

    // Get visits within a date range (Admin Portal: reports)
    public List<Visit> getVisitsByDateRange(LocalDate start, LocalDate end) {
        return visitRepository.findByVisitDateBetween(start, end);
    }

    // Record a new visit (Doctor Portal)
    public Visit recordVisit(Long patientId, Long doctorId, LocalDate visitDate,
                              String reason, String diagnosis, String notes,
                              String visitType) {

        // Verify patient exists
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // Verify doctor exists
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setDoctor(doctor);
        visit.setVisitDate(visitDate);
        visit.setReason(reason);
        visit.setDiagnosis(diagnosis);
        visit.setNotes(notes);
        visit.setVisitType(visitType);

        return visitRepository.save(visit);
    }

    // Update visit notes/diagnosis (Doctor Portal)
    public Visit updateVisit(Long id, String diagnosis, String notes) {
        Visit visit = getVisitById(id);
        visit.setDiagnosis(diagnosis);
        visit.setNotes(notes);
        return visitRepository.save(visit);
    }

    // Delete a visit (Admin Portal)
    public void deleteVisit(Long id) {
        visitRepository.deleteById(id);
    }
}
