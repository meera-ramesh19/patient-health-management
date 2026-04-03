package com.labservice.service;

import com.labservice.exception.ResourceNotFoundException;
import com.labservice.model.Doctor;
import com.labservice.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    // Get all doctors (Admin Portal, Patient Portal)
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    // Get one doctor by ID
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
    }

    // Get doctor profile by user ID (after login: "find my doctor profile")
    public Doctor getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user: " + userId));
    }

    // Find doctors by specialization (Patient Portal: "show me cardiologists")
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    // Search doctors by name
    public List<Doctor> searchByName(String name) {
        return doctorRepository.findByNameContainingIgnoreCase(name);
    }

    // Create a new doctor profile (Admin Portal)
    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    // Update doctor profile (Doctor Portal: update own profile)
    public Doctor updateDoctor(Long id, Doctor updatedData) {
        Doctor doctor = getDoctorById(id);
        doctor.setName(updatedData.getName());
        doctor.setEmail(updatedData.getEmail());
        doctor.setSpecialization(updatedData.getSpecialization());
        doctor.setPhoneNumber(updatedData.getPhoneNumber());
        doctor.setHospitalAffiliation(updatedData.getHospitalAffiliation());
        doctor.setYearsOfExperience(updatedData.getYearsOfExperience());
        doctor.setConsultationFee(updatedData.getConsultationFee());
        doctor.setAddress(updatedData.getAddress());
        return doctorRepository.save(doctor);
    }

    // Delete a doctor (Admin Portal)
    public void deleteDoctor(Long id) {
        doctorRepository.deleteById(id);
    }
}
