package com.labservice.service;

import com.labservice.exception.BadRequestException;
import com.labservice.exception.ResourceNotFoundException;
import com.labservice.model.Doctor;
import com.labservice.model.LabOrder;
import com.labservice.model.Patient;
import com.labservice.repository.DoctorRepository;
import com.labservice.repository.LabOrderRepository;
import com.labservice.repository.PatientRepository;
import com.labservice.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// ─────────────────────────────────────────────────────────────
// UNIT TEST FOR LabOrderService
// ─────────────────────────────────────────────────────────────
// Tests the status lifecycle rules:
//   - ORDERED → IN_PROGRESS → COMPLETED
//   - Can't change status of COMPLETED/CANCELLED orders
// ─────────────────────────────────────────────────────────────

@ExtendWith(MockitoExtension.class)
class LabOrderServiceTest {

    @Mock private LabOrderRepository labOrderRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private VisitRepository visitRepository;

    @InjectMocks
    private LabOrderService labOrderService;

    private LabOrder testLabOrder;

    @BeforeEach
    void setUp() {
        testLabOrder = new LabOrder();
        testLabOrder.setId(1L);
        testLabOrder.setTestName("Blood Panel");
        testLabOrder.setStatus("ORDERED");
    }

    @Test
    void updateStatus_orderedToInProgress_succeeds() {
        when(labOrderRepository.findById(1L)).thenReturn(Optional.of(testLabOrder));
        when(labOrderRepository.save(any(LabOrder.class))).thenReturn(testLabOrder);

        LabOrder result = labOrderService.updateStatus(1L, "IN_PROGRESS");

        assertEquals("IN_PROGRESS", result.getStatus());
    }

    @Test
    void updateStatus_completedOrder_throwsBadRequest() {
        testLabOrder.setStatus("COMPLETED");
        when(labOrderRepository.findById(1L)).thenReturn(Optional.of(testLabOrder));

        assertThrows(BadRequestException.class,
                () -> labOrderService.updateStatus(1L, "IN_PROGRESS"));
    }

    @Test
    void updateStatus_cancelledOrder_throwsBadRequest() {
        testLabOrder.setStatus("CANCELLED");
        when(labOrderRepository.findById(1L)).thenReturn(Optional.of(testLabOrder));

        assertThrows(BadRequestException.class,
                () -> labOrderService.updateStatus(1L, "IN_PROGRESS"));
    }

    @Test
    void addResults_setsResultsAndCompletes() {
        when(labOrderRepository.findById(1L)).thenReturn(Optional.of(testLabOrder));
        when(labOrderRepository.save(any(LabOrder.class))).thenReturn(testLabOrder);

        LabOrder result = labOrderService.addResults(1L, "All levels normal");

        assertEquals("All levels normal", result.getResults());
        assertEquals("COMPLETED", result.getStatus());
        assertNotNull(result.getCompletedDate());
    }

    @Test
    void getLabOrderById_notFound_throwsException() {
        when(labOrderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> labOrderService.getLabOrderById(99L));
    }

    @Test
    void createLabOrder_validData_createsOrder() {
        Patient patient = new Patient();
        patient.setId(1L);
        Doctor doctor = new Doctor();
        doctor.setId(1L);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(labOrderRepository.save(any(LabOrder.class))).thenAnswer(i -> i.getArgument(0));

        LabOrder result = labOrderService.createLabOrder(1L, 1L, null, "CBC", "CBC-001", "Routine");

        assertEquals("CBC", result.getTestName());
        assertEquals("ORDERED", result.getStatus());
        assertNotNull(result.getOrderedDate());
    }
}
