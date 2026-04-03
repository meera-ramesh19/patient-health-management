package com.labservice.service;

import com.labservice.exception.BadRequestException;
import com.labservice.exception.ResourceNotFoundException;
import com.labservice.model.Doctor;
import com.labservice.model.LabOrder;
import com.labservice.model.Patient;
import com.labservice.model.Visit;
import com.labservice.repository.DoctorRepository;
import com.labservice.repository.LabOrderRepository;
import com.labservice.repository.PatientRepository;
import com.labservice.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class LabOrderService {

    @Autowired
    private LabOrderRepository labOrderRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private VisitRepository visitRepository;

    // Get all lab orders (Admin Portal)
    public List<LabOrder> getAllLabOrders() {
        return labOrderRepository.findAll();
    }

    // Get one lab order by ID
    public LabOrder getLabOrderById(Long id) {
        return labOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Lab order not found with id: " + id));
    }

    // Get all lab orders for a patient (Patient Portal: "my lab results")
    public List<LabOrder> getLabOrdersByPatient(Long patientId) {
        return labOrderRepository.findByPatientId(patientId);
    }

    // Get lab orders by status (Admin Portal: "show all pending labs")
    public List<LabOrder> getLabOrdersByStatus(String status) {
        return labOrderRepository.findByStatus(status);
    }

    // Get lab orders for a patient filtered by status
    // Patient Portal: "show my completed labs"
    public List<LabOrder> getLabOrdersByPatientAndStatus(Long patientId, String status) {
        return labOrderRepository.findByPatientIdAndStatus(patientId, status);
    }

    // Get lab orders placed by a specific doctor (Doctor Portal)
    public List<LabOrder> getLabOrdersByDoctor(Long doctorId) {
        return labOrderRepository.findByOrderedById(doctorId);
    }

    // Order a new lab test (Doctor Portal)
    public LabOrder createLabOrder(Long patientId, Long doctorId, Long visitId,
                                    String testName, String testCode, String notes) {

        // Verify patient exists
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // Verify doctor exists
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        LabOrder labOrder = new LabOrder();
        labOrder.setPatient(patient);
        labOrder.setOrderedBy(doctor);
        labOrder.setTestName(testName);
        labOrder.setTestCode(testCode);
        labOrder.setNotes(notes);
        labOrder.setOrderedDate(LocalDate.now());
        labOrder.setStatus("ORDERED");

        // Link to visit if provided (optional)
        if (visitId != null) {
            Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found"));
            labOrder.setVisit(visit);
        }

        return labOrderRepository.save(labOrder);
    }

    // Update lab order status (Doctor Portal)
    // ORDERED → IN_PROGRESS → COMPLETED or CANCELLED
    public LabOrder updateStatus(Long id, String newStatus) {
        LabOrder labOrder = getLabOrderById(id);

        // Business rule: can't change status of a completed/cancelled order
        String currentStatus = labOrder.getStatus();
        if ("COMPLETED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            throw new BadRequestException("Cannot change status of a " + currentStatus + " lab order");
        }

        labOrder.setStatus(newStatus);

        // If completed, set the completion date
        if ("COMPLETED".equals(newStatus)) {
            labOrder.setCompletedDate(LocalDate.now());
        }

        return labOrderRepository.save(labOrder);
    }

    // Add results to a completed lab order
    public LabOrder addResults(Long id, String results) {
        LabOrder labOrder = getLabOrderById(id);
        labOrder.setResults(results);
        labOrder.setStatus("COMPLETED");
        labOrder.setCompletedDate(LocalDate.now());
        return labOrderRepository.save(labOrder);
    }

    // Delete a lab order
    public void deleteLabOrder(Long id) {
        labOrderRepository.deleteById(id);
    }
}
