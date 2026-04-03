package com.labservice.controller.admin;

import com.labservice.dto.response.AdminDashboardResponse;
import com.labservice.model.*;
import com.labservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminPortalController {

    @Autowired
    private UserService userService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private LabOrderService labOrderService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PharmacyService pharmacyService;

    @Autowired
    private DashboardService dashboardService;

    // ==================== DASHBOARD ====================

    // GET /api/admin/dashboard — "Show system-wide stats"
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    // ==================== USER MANAGEMENT ====================

    // GET /api/admin/users — "Show all user accounts"
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET /api/admin/users/1 — "Show one user"
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // PUT /api/admin/users/1/enable — "Approve this account"
    @PutMapping("/users/{id}/enable")
    public ResponseEntity<User> enableUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.enableUser(id));
    }

    // PUT /api/admin/users/1/disable — "Suspend this account"
    @PutMapping("/users/{id}/disable")
    public ResponseEntity<User> disableUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.disableUser(id));
    }

    // PUT /api/admin/users/1/role — "Change this user's role"
    @PutMapping("/users/{id}/role")
    public ResponseEntity<User> changeUserRole(
            @PathVariable Long id,
            @RequestBody Role newRole) {
        return ResponseEntity.ok(userService.changeRole(id, newRole));
    }

    // DELETE /api/admin/users/1 — "Delete this account"
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== PATIENT MANAGEMENT ====================

    // GET /api/admin/patients — "Show all patients"
    @GetMapping("/patients")
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    // GET /api/admin/patients/5 — "Show one patient"
    @GetMapping("/patients/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    // GET /api/admin/patients/search?name=Smith — "Search patients by last name"
    @GetMapping("/patients/search")
    public ResponseEntity<List<Patient>> searchPatients(@RequestParam String name) {
        return ResponseEntity.ok(patientService.searchByLastName(name));
    }

    // POST /api/admin/patients — "Create a patient profile"
    @PostMapping("/patients")
    public ResponseEntity<Patient> createPatient(@RequestBody Patient patient) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.createPatient(patient));
    }

    // PUT /api/admin/patients/5 — "Update a patient's info"
    @PutMapping("/patients/{id}")
    public ResponseEntity<Patient> updatePatient(
            @PathVariable Long id,
            @RequestBody Patient updatedData) {
        return ResponseEntity.ok(patientService.updatePatient(id, updatedData));
    }

    // PUT /api/admin/patients/5/assign-doctor/3 — "Assign doctor #3 to patient #5"
    @PutMapping("/patients/{patientId}/assign-doctor/{doctorId}")
    public ResponseEntity<Patient> assignDoctor(
            @PathVariable Long patientId,
            @PathVariable Long doctorId) {
        return ResponseEntity.ok(patientService.assignDoctor(patientId, doctorId));
    }

    // DELETE /api/admin/patients/5 — "Delete a patient"
    @DeleteMapping("/patients/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== DOCTOR MANAGEMENT ====================

    // GET /api/admin/doctors — "Show all doctors"
    @GetMapping("/doctors")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    // GET /api/admin/doctors/3 — "Show one doctor"
    @GetMapping("/doctors/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    // GET /api/admin/doctors/search?name=Jones — "Search doctors by name"
    @GetMapping("/doctors/search")
    public ResponseEntity<List<Doctor>> searchDoctors(@RequestParam String name) {
        return ResponseEntity.ok(doctorService.searchByName(name));
    }

    // POST /api/admin/doctors — "Add a new doctor"
    @PostMapping("/doctors")
    public ResponseEntity<Doctor> createDoctor(@RequestBody Doctor doctor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.createDoctor(doctor));
    }

    // PUT /api/admin/doctors/3 — "Update a doctor's info"
    @PutMapping("/doctors/{id}")
    public ResponseEntity<Doctor> updateDoctor(
            @PathVariable Long id,
            @RequestBody Doctor updatedData) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, updatedData));
    }

    // DELETE /api/admin/doctors/3 — "Remove a doctor"
    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== VISITS ====================

    // GET /api/admin/visits — "Show all visits system-wide"
    @GetMapping("/visits")
    public ResponseEntity<List<Visit>> getAllVisits() {
        return ResponseEntity.ok(visitService.getAllVisits());
    }

    // GET /api/admin/visits/10 — "Show one visit"
    @GetMapping("/visits/{id}")
    public ResponseEntity<Visit> getVisitById(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.getVisitById(id));
    }

    // GET /api/admin/visits/range?start=2025-01-01&end=2025-12-31 — "Visits in date range"
    @GetMapping("/visits/range")
    public ResponseEntity<List<Visit>> getVisitsByDateRange(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        return ResponseEntity.ok(visitService.getVisitsByDateRange(start, end));
    }

    // DELETE /api/admin/visits/10 — "Delete a visit record"
    @DeleteMapping("/visits/{id}")
    public ResponseEntity<Void> deleteVisit(@PathVariable Long id) {
        visitService.deleteVisit(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== PRESCRIPTIONS ====================

    // GET /api/admin/prescriptions — "Show all prescriptions"
    @GetMapping("/prescriptions")
    public ResponseEntity<List<Prescription>> getAllPrescriptions() {
        return ResponseEntity.ok(prescriptionService.getAllPrescriptions());
    }

    // GET /api/admin/prescriptions/7 — "Show one prescription"
    @GetMapping("/prescriptions/{id}")
    public ResponseEntity<Prescription> getPrescriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionById(id));
    }

    // DELETE /api/admin/prescriptions/7 — "Delete a prescription"
    @DeleteMapping("/prescriptions/{id}")
    public ResponseEntity<Void> deletePrescription(@PathVariable Long id) {
        prescriptionService.deletePrescription(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== LAB ORDERS ====================

    // GET /api/admin/lab-orders — "Show all lab orders"
    @GetMapping("/lab-orders")
    public ResponseEntity<List<LabOrder>> getAllLabOrders() {
        return ResponseEntity.ok(labOrderService.getAllLabOrders());
    }

    // GET /api/admin/lab-orders/8 — "Show one lab order"
    @GetMapping("/lab-orders/{id}")
    public ResponseEntity<LabOrder> getLabOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(labOrderService.getLabOrderById(id));
    }

    // GET /api/admin/lab-orders/status/ORDERED — "Show all pending labs"
    @GetMapping("/lab-orders/status/{status}")
    public ResponseEntity<List<LabOrder>> getLabOrdersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(labOrderService.getLabOrdersByStatus(status));
    }

    // DELETE /api/admin/lab-orders/8 — "Delete a lab order"
    @DeleteMapping("/lab-orders/{id}")
    public ResponseEntity<Void> deleteLabOrder(@PathVariable Long id) {
        labOrderService.deleteLabOrder(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== APPOINTMENTS ====================

    // GET /api/admin/appointments — "Show all appointments"
    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    // GET /api/admin/appointments/12 — "Show one appointment"
    @GetMapping("/appointments/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    // PUT /api/admin/appointments/12/cancel — "Cancel an appointment"
    @PutMapping("/appointments/{id}/cancel")
    public ResponseEntity<Appointment> cancelAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id));
    }

    // DELETE /api/admin/appointments/12 — "Delete an appointment"
    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== PHARMACIES ====================

    // GET /api/admin/pharmacies — "Show all pharmacies"
    @GetMapping("/pharmacies")
    public ResponseEntity<List<Pharmacy>> getAllPharmacies() {
        return ResponseEntity.ok(pharmacyService.getAllPharmacies());
    }

    // GET /api/admin/pharmacies/2 — "Show one pharmacy"
    @GetMapping("/pharmacies/{id}")
    public ResponseEntity<Pharmacy> getPharmacyById(@PathVariable Long id) {
        return ResponseEntity.ok(pharmacyService.getPharmacyById(id));
    }

    // GET /api/admin/pharmacies/search?name=CVS — "Search pharmacies by name"
    @GetMapping("/pharmacies/search")
    public ResponseEntity<List<Pharmacy>> searchPharmacies(@RequestParam String name) {
        return ResponseEntity.ok(pharmacyService.searchByName(name));
    }

    // POST /api/admin/pharmacies — "Add a new pharmacy"
    @PostMapping("/pharmacies")
    public ResponseEntity<Pharmacy> createPharmacy(@RequestBody Pharmacy pharmacy) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pharmacyService.createPharmacy(pharmacy));
    }

    // PUT /api/admin/pharmacies/2 — "Update pharmacy info"
    @PutMapping("/pharmacies/{id}")
    public ResponseEntity<Pharmacy> updatePharmacy(
            @PathVariable Long id,
            @RequestBody Pharmacy updatedData) {
        return ResponseEntity.ok(pharmacyService.updatePharmacy(id, updatedData));
    }

    // DELETE /api/admin/pharmacies/2 — "Remove a pharmacy"
    @DeleteMapping("/pharmacies/{id}")
    public ResponseEntity<Void> deletePharmacy(@PathVariable Long id) {
        pharmacyService.deletePharmacy(id);
        return ResponseEntity.noContent().build();
    }
}
