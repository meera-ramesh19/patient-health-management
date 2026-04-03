package com.labservice.service;

import com.labservice.exception.ResourceNotFoundException;
import com.labservice.model.Doctor;
import com.labservice.model.Patient;
import com.labservice.repository.DoctorRepository;
import com.labservice.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    // Get all patients (Admin Portal)
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    // Get one patient by ID
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }

    // Get patient profile by user ID (after login: "find my patient profile")
    public Patient getPatientByUserId(Long userId) {
        return patientRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId));
    }

    // Get all patients assigned to a specific doctor (Doctor Portal)
    public List<Patient> getPatientsByDoctor(Long doctorId) {
        return patientRepository.findByPrimaryDoctorId(doctorId);
    }

    // Search patients by last name
    public List<Patient> searchByLastName(String name) {
        return patientRepository.findByLastNameContainingIgnoreCase(name);
    }

    // Save a new patient (used during registration)
    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    // Update patient profile
    public Patient updatePatient(Long id, Patient updatedData) {
        Patient patient = getPatientById(id);
        patient.setFirstName(updatedData.getFirstName());
        patient.setLastName(updatedData.getLastName());
        patient.setEmail(updatedData.getEmail());
        patient.setPhoneNumber(updatedData.getPhoneNumber());
        patient.setDateOfBirth(updatedData.getDateOfBirth());
        patient.setAddress(updatedData.getAddress());
        return patientRepository.save(patient);
    }

    // Assign a primary doctor to a patient (Admin Portal)
    public Patient assignDoctor(Long patientId, Long doctorId) {
        Patient patient = getPatientById(patientId);
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));
        patient.setPrimaryDoctor(doctor);
        return patientRepository.save(patient);
    }

    // Delete a patient
    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }
}
