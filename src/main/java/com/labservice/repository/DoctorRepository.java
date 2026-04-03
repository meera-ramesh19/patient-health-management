package com.labservice.repository;

import com.labservice.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // Find doctor profile linked to a User account
    // Used after login: "user logged in, now find their doctor profile"
    Optional<Doctor> findByUserId(Long userId);

    // Find all doctors with a specific specialization
    // Used in Patient Portal: "show me all cardiologists"
    List<Doctor> findBySpecialization(String specialization);

    // Search doctors by name (case-insensitive, partial match)
    List<Doctor> findByNameContainingIgnoreCase(String name);
}
