package com.labservice.controller.patient;

import com.labservice.dto.response.PatientDashboardResponse;
import com.labservice.exception.UnauthorizedException;
import com.labservice.model.*;
import com.labservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientPortalController {

    @Autowired
    private UserService userService;

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
    // HELPER: Get the logged-in patient from the JWT token
    // ─────────────────────────────────────────────
    // This is the KEY to data-level security.
    // Instead of trusting the URL (/visits/5 — anyone can type 5),
    // we read the username from the JWT token (can't be faked)
    // and look up THEIR patient record.
    //
    // Every endpoint below calls this method — so patients can
    // ONLY ever see their own data.
    private Patient getLoggedInPatient() {
        // Step 1: Get username from the JWT token
        // SecurityContextHolder was set by JwtAuthenticationFilter
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        // Step 2: Find the User record
        User user = userService.getUserByUsername(username);

        // Step 3: Find the Patient profile linked to this User
        return patientService.getPatientByUserId(user.getId());
    }

    // ==================== DASHBOARD ====================

    // GET /api/patient/dashboard — "Show me my dashboard"
    @GetMapping("/dashboard")
    public ResponseEntity<PatientDashboardResponse> getDashboard() {
        Patient me = getLoggedInPatient();
        return ResponseEntity.ok(dashboardService.getPatientDashboard(me.getId()));
    }

    // ==================== PROFILE ====================

    // GET /api/patient/profile — "Show me my profile"
    // BEFORE: /profile/{patientId} — any patient could type any ID
    // AFTER:  /profile — server reads YOUR ID from the token
    @GetMapping("/profile")
    public ResponseEntity<Patient> getMyProfile() {
        return ResponseEntity.ok(getLoggedInPatient());
    }

    // PUT /api/patient/profile — "Update my phone number / address"
    @PutMapping("/profile")
    public ResponseEntity<Patient> updateMyProfile(@RequestBody Patient updatedData) {
        Patient me = getLoggedInPatient();
        return ResponseEntity.ok(patientService.updatePatient(me.getId(), updatedData));
    }

    // ==================== VISITS ====================

    // GET /api/patient/visits — "Show me all my past doctor visits"
    @GetMapping("/visits")
    public ResponseEntity<List<Visit>> getMyVisits() {
        Patient me = getLoggedInPatient();
        return ResponseEntity.ok(visitService.getVisitsByPatient(me.getId()));
    }

    // ==================== PRESCRIPTIONS ====================

    // GET /api/patient/prescriptions — "Show me all my prescriptions"
    @GetMapping("/prescriptions")
    public ResponseEntity<List<Prescription>> getMyPrescriptions() {
        Patient me = getLoggedInPatient();
        return ResponseEntity.ok(prescriptionService.getPrescriptionsByPatient(me.getId()));
    }

    // GET /api/patient/prescriptions/active — "Show me only my current prescriptions"
    @GetMapping("/prescriptions/active")
    public ResponseEntity<List<Prescription>> getActivePrescriptions() {
        return ResponseEntity.ok(prescriptionService.getActivePrescriptions());
    }

    // ==================== LAB ORDERS ====================

    // GET /api/patient/lab-orders — "Show me all my lab orders"
    @GetMapping("/lab-orders")
    public ResponseEntity<List<LabOrder>> getMyLabOrders() {
        Patient me = getLoggedInPatient();
        return ResponseEntity.ok(labOrderService.getLabOrdersByPatient(me.getId()));
    }

    // GET /api/patient/lab-orders/status/COMPLETED — "Show me just my completed labs"
    @GetMapping("/lab-orders/status/{status}")
    public ResponseEntity<List<LabOrder>> getMyLabOrdersByStatus(@PathVariable String status) {
        Patient me = getLoggedInPatient();
        return ResponseEntity.ok(labOrderService.getLabOrdersByPatientAndStatus(me.getId(), status));
    }

    // ==================== APPOINTMENTS ====================

    // GET /api/patient/appointments — "Show me my upcoming appointments"
    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getMyAppointments() {
        Patient me = getLoggedInPatient();
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(me.getId()));
    }

    // POST /api/patient/appointments — "Book a new appointment"
    // Still uses @RequestBody for the doctor ID, dateTime, and reason
    // But forces the patient ID to be the logged-in user (can't book for someone else)
    @PostMapping("/appointments")
    public ResponseEntity<Appointment> bookAppointment(@RequestBody Appointment appointment) {
        Patient me = getLoggedInPatient();
        Appointment saved = appointmentService.bookAppointment(
                me.getId(),                           // ALWAYS the logged-in patient
                appointment.getDoctor().getId(),
                appointment.getDateTime(),
                appointment.getReason());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // DELETE /api/patient/appointments/{appointmentId} — "Cancel my appointment"
    // Keeps the appointmentId in URL (we need to know WHICH appointment)
    // But verifies it belongs to the logged-in patient before cancelling
    @DeleteMapping("/appointments/{appointmentId}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long appointmentId) {
        Patient me = getLoggedInPatient();
        // Verify this appointment belongs to the logged-in patient
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        if (!appointment.getPatient().getId().equals(me.getId())) {
            throw new UnauthorizedException("You can only cancel your own appointments");
        }
        appointmentService.cancelAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }

    // ==================== DOCTORS ====================

    // GET /api/patient/doctors — "Show me my primary doctor's info"
    @GetMapping("/doctors")
    public ResponseEntity<Doctor> getMyDoctor() {
        Patient me = getLoggedInPatient();
        return ResponseEntity.ok(me.getPrimaryDoctor());
    }
}
