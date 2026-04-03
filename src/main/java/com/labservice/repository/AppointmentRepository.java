package com.labservice.repository;

import com.labservice.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Find all appointments for a specific patient
    // Used in Patient Portal: "show me my appointments"
    List<Appointment> findByPatientId(Long patientId);

    // Find all appointments for a specific doctor
    // Used in Doctor Portal: "show me my schedule"
    List<Appointment> findByDoctorId(Long doctorId);

    // Check if a doctor already has an appointment at a specific time
    // Used for conflict detection: "is this time slot taken?"
    boolean existsByDoctorIdAndDateTime(Long doctorId, LocalDateTime dateTime);

    // Find appointments for a patient filtered by status
    // Used in Patient Portal: "show me my upcoming (SCHEDULED) appointments"
    List<Appointment> findByPatientIdAndStatus(Long patientId, String status);

    // Find a doctor's appointments within a time range
    // Used in Doctor Portal: "show me today's schedule"
    List<Appointment> findByDoctorIdAndDateTimeBetween(
        Long doctorId, LocalDateTime start, LocalDateTime end);
}
