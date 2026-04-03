package com.labservice.repository;

import com.labservice.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Find patient profile linked to a User account
    // Used after login: "user logged in, now find their patient profile"
    Optional<Patient> findByUserId(Long userId);

    // Find all patients assigned to a specific doctor
    // Used in Doctor Portal: "show me my patients"
    List<Patient> findByPrimaryDoctorId(Long doctorId);

    // Search patients by last name (case-insensitive, partial match)
    // "Smi" matches "Smith", "Smithson", etc.
    List<Patient> findByLastNameContainingIgnoreCase(String name);
}
