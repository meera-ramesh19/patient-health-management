package com.labservice.repository;

import com.labservice.model.LabOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {

    // Find all lab orders for a specific patient
    // Used in Patient Portal: "show me my lab results"
    List<LabOrder> findByPatientId(Long patientId);

    // Find lab orders by status
    // Used in Admin Portal: "show me all pending lab orders"
    List<LabOrder> findByStatus(String status);

    // Find lab orders for a patient filtered by status
    // Used in Patient Portal: "show me my completed labs"
    List<LabOrder> findByPatientIdAndStatus(Long patientId, String status);

    // Find all lab orders placed by a specific doctor
    // Used in Doctor Portal: "show me labs I've ordered"
    List<LabOrder> findByOrderedById(Long doctorId);
}
