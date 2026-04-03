package com.labservice.repository;

import com.labservice.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    // Find all visits for a specific patient
    // Used in Patient Portal: "show me my visit history"
    List<Visit> findByPatientId(Long patientId);

    // Find all visits conducted by a specific doctor
    // Used in Doctor Portal: "show me visits I've conducted"
    List<Visit> findByDoctorId(Long doctorId);

    // Find patient's visits sorted newest first
    // "OrderBy...Desc" = descending order (most recent at top)
    List<Visit> findByPatientIdOrderByVisitDateDesc(Long patientId);

    // Find visits within a date range
    // Used in Admin Portal: "show visits from March 1 to March 31"
    List<Visit> findByVisitDateBetween(LocalDate start, LocalDate end);
}
