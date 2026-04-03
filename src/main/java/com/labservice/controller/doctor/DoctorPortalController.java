package com.labservice.controller.doctor;

import com.labservice.dto.response.DoctorDashboardResponse;
import com.labservice.model.*;
import com.labservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorPortalController {

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private LabOrderService labOrderService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DashboardService dashboardService;

    // ─────────────────────────────────────────────
    // HELPER: Get the logged-in doctor from the JWT token
    // ─────────────────────────────────────────────
    // Same pattern as PatientPortalController:
    // Read username from token → find User → find Doctor profile
    private Doctor getLoggedInDoctor() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userService.getUserByUsername(username);
        return doctorService.getDoctorByUserId(user.getId());
    }

    // ==================== DASHBOARD ====================

    // GET /api/doctor/dashboard — "Show me my dashboard"
    @GetMapping("/dashboard")
    public ResponseEntity<DoctorDashboardResponse> getDashboard() {
        Doctor me = getLoggedInDoctor();
        return ResponseEntity.ok(dashboardService.getDoctorDashboard(me.getId()));
    }

    // ==================== PROFILE ====================

    // GET /api/doctor/profile — "Show my profile"
    @GetMapping("/profile")
    public ResponseEntity<Doctor> getMyProfile() {
        return ResponseEntity.ok(getLoggedInDoctor());
    }

    // PUT /api/doctor/profile — "Update my specialization / phone / address"
    @PutMapping("/profile")
    public ResponseEntity<Doctor> updateMyProfile(@RequestBody Doctor updatedData) {
        Doctor me = getLoggedInDoctor();
        return ResponseEntity.ok(doctorService.updateDoctor(me.getId(), updatedData));
    }

    // ==================== MY PATIENTS ====================

    // GET /api/doctor/patients — "Show all patients assigned to me"
    @GetMapping("/patients")
    public ResponseEntity<List<Patient>> getMyPatients() {
        Doctor me = getLoggedInDoctor();
        return ResponseEntity.ok(patientService.getPatientsByDoctor(me.getId()));
    }

    // ==================== VISITS ====================

    // GET /api/doctor/visits — "Show all visits I conducted"
    @GetMapping("/visits")
    public ResponseEntity<List<Visit>> getMyVisits() {
        Doctor me = getLoggedInDoctor();
        return ResponseEntity.ok(visitService.getVisitsByDoctor(me.getId()));
    }

    // POST /api/doctor/visits — "Record a new visit"
    // Forces the doctor ID to be the logged-in doctor
    // The patient ID still comes from the request body (doctor picks which patient)
    @PostMapping("/visits")
    public ResponseEntity<Visit> recordVisit(@RequestBody Visit visit) {
        Doctor me = getLoggedInDoctor();
        Visit saved = visitService.recordVisit(
                visit.getPatient().getId(),
                me.getId(),                           // ALWAYS the logged-in doctor
                visit.getVisitDate(),
                visit.getReason(),
                visit.getDiagnosis(),
                visit.getNotes(),
                visit.getVisitType());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /api/doctor/visits/{visitId} — "Update visit diagnosis / notes"
    @PutMapping("/visits/{visitId}")
    public ResponseEntity<Visit> updateVisit(
            @PathVariable Long visitId,
            @RequestBody Visit updatedData) {
        return ResponseEntity.ok(visitService.updateVisit(
                visitId,
                updatedData.getDiagnosis(),
                updatedData.getNotes()));
    }

    // ==================== PRESCRIPTIONS ====================

    // GET /api/doctor/prescriptions — "Show all prescriptions I wrote"
    @GetMapping("/prescriptions")
    public ResponseEntity<List<Prescription>> getMyPrescriptions() {
        Doctor me = getLoggedInDoctor();
        return ResponseEntity.ok(prescriptionService.getPrescriptionsByDoctor(me.getId()));
    }

    // POST /api/doctor/prescriptions — "Write a new prescription"
    // Forces the doctor ID to be the logged-in doctor
    @PostMapping("/prescriptions")
    public ResponseEntity<Prescription> writePrescription(@RequestBody Prescription prescription) {
        Doctor me = getLoggedInDoctor();
        Prescription saved = prescriptionService.createPrescription(
                prescription.getVisit().getId(),
                me.getId(),                           // ALWAYS the logged-in doctor
                prescription.getMedicationName(),
                prescription.getDosage(),
                prescription.getFrequency(),
                prescription.getStartDate(),
                prescription.getEndDate(),
                prescription.getInstructions());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ==================== LAB ORDERS ====================

    // GET /api/doctor/lab-orders — "Show all labs I ordered"
    @GetMapping("/lab-orders")
    public ResponseEntity<List<LabOrder>> getMyLabOrders() {
        Doctor me = getLoggedInDoctor();
        return ResponseEntity.ok(labOrderService.getLabOrdersByDoctor(me.getId()));
    }

    // POST /api/doctor/lab-orders — "Order a new lab test"
    // Forces the doctor ID to be the logged-in doctor
    @PostMapping("/lab-orders")
    public ResponseEntity<LabOrder> orderLab(@RequestBody LabOrder labOrder) {
        Doctor me = getLoggedInDoctor();
        Long visitId = labOrder.getVisit() != null ? labOrder.getVisit().getId() : null;
        LabOrder saved = labOrderService.createLabOrder(
                labOrder.getPatient().getId(),
                me.getId(),                           // ALWAYS the logged-in doctor
                visitId,
                labOrder.getTestName(),
                labOrder.getTestCode(),
                labOrder.getNotes());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /api/doctor/lab-orders/{labOrderId}/status — "Update lab status"
    @PutMapping("/lab-orders/{labOrderId}/status")
    public ResponseEntity<LabOrder> updateLabStatus(
            @PathVariable Long labOrderId,
            @RequestBody String status) {
        return ResponseEntity.ok(labOrderService.updateStatus(labOrderId, status));
    }

    // PUT /api/doctor/lab-orders/{labOrderId}/results — "Add results to a lab"
    @PutMapping("/lab-orders/{labOrderId}/results")
    public ResponseEntity<LabOrder> addLabResults(
            @PathVariable Long labOrderId,
            @RequestBody String results) {
        return ResponseEntity.ok(labOrderService.addResults(labOrderId, results));
    }

    // ==================== APPOINTMENTS ====================

    // GET /api/doctor/appointments — "Show my schedule"
    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getMyAppointments() {
        Doctor me = getLoggedInDoctor();
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(me.getId()));
    }
}
