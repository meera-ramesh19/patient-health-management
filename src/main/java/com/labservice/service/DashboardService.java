package com.labservice.service;

import com.labservice.dto.response.AdminDashboardResponse;
import com.labservice.dto.response.DoctorDashboardResponse;
import com.labservice.dto.response.PatientDashboardResponse;
import com.labservice.model.Appointment;
import com.labservice.model.LabOrder;
import com.labservice.model.Prescription;
import com.labservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// ─────────────────────────────────────────────────────────────
// WHY THIS CLASS EXISTS
// ─────────────────────────────────────────────────────────────
// Each portal has a dashboard — a summary page showing key stats.
// This service gathers data from multiple repositories and
// assembles it into dashboard response DTOs.
//
// Patient Dashboard: "How many appointments do I have? Any active meds?"
// Doctor Dashboard:  "How many patients? What's on my schedule today?"
// Admin Dashboard:   "System-wide counts: users, patients, doctors, etc."
//
// Each method takes an ID (patient/doctor) or nothing (admin)
// and returns a pre-built dashboard response.
// ─────────────────────────────────────────────────────────────

@Service
public class DashboardService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private LabOrderRepository labOrderRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    // ─────────────────────────────────────────────
    // PATIENT DASHBOARD
    // ─────────────────────────────────────────────
    public PatientDashboardResponse getPatientDashboard(Long patientId) {
        PatientDashboardResponse dashboard = new PatientDashboardResponse();

        // Count upcoming appointments (status = SCHEDULED)
        List<Appointment> scheduled = appointmentRepository
                .findByPatientIdAndStatus(patientId, "SCHEDULED");
        dashboard.setUpcomingAppointments(scheduled.size());

        // Find the next appointment (soonest future one)
        // .stream() = iterate over the list
        // .filter() = keep only appointments after right now
        // .min() = find the earliest one
        // .orElse(null) = if no future appointments, set to null
        Appointment next = scheduled.stream()
                .filter(a -> a.getDateTime().isAfter(LocalDateTime.now()))
                .min((a, b) -> a.getDateTime().compareTo(b.getDateTime()))
                .orElse(null);
        dashboard.setNextAppointment(next);

        // Active prescriptions (end date hasn't passed yet, or no end date)
        List<Prescription> allPatientRx = prescriptionRepository
                .findByVisitPatientId(patientId);
        List<Prescription> activeRx = allPatientRx.stream()
                .filter(p -> p.getEndDate() == null || p.getEndDate().isAfter(LocalDate.now()))
                .collect(Collectors.toList());
        dashboard.setActivePrescriptions(activeRx.size());
        dashboard.setCurrentMedications(activeRx);

        // Pending lab orders (status is not COMPLETED or CANCELLED)
        List<LabOrder> allLabs = labOrderRepository.findByPatientId(patientId);
        List<LabOrder> pendingLabs = allLabs.stream()
                .filter(l -> !"COMPLETED".equals(l.getStatus()) && !"CANCELLED".equals(l.getStatus()))
                .collect(Collectors.toList());
        dashboard.setPendingLabOrders(pendingLabs.size());

        // Recent lab orders (last 5)
        List<LabOrder> recentLabs = allLabs.stream()
                .sorted((a, b) -> b.getOrderedDate().compareTo(a.getOrderedDate()))
                .limit(5)
                .collect(Collectors.toList());
        dashboard.setRecentLabOrders(recentLabs);

        return dashboard;
    }

    // ─────────────────────────────────────────────
    // DOCTOR DASHBOARD
    // ─────────────────────────────────────────────
    public DoctorDashboardResponse getDoctorDashboard(Long doctorId) {
        DoctorDashboardResponse dashboard = new DoctorDashboardResponse();

        // Total assigned patients
        List<?> patients = patientRepository.findByPrimaryDoctorId(doctorId);
        dashboard.setTotalPatients(patients.size());

        // Upcoming appointments
        List<Appointment> allAppointments = appointmentRepository.findByDoctorId(doctorId);
        List<Appointment> upcoming = allAppointments.stream()
                .filter(a -> "SCHEDULED".equals(a.getStatus()))
                .filter(a -> a.getDateTime().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
        dashboard.setUpcomingAppointments(upcoming.size());

        // Today's appointments
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        List<Appointment> todays = appointmentRepository
                .findByDoctorIdAndDateTimeBetween(doctorId, startOfDay, endOfDay);
        dashboard.setTodaysAppointments(todays);

        // Pending lab orders (ordered by this doctor, not yet completed)
        List<LabOrder> allLabs = labOrderRepository.findByOrderedById(doctorId);
        List<LabOrder> pendingLabs = allLabs.stream()
                .filter(l -> !"COMPLETED".equals(l.getStatus()) && !"CANCELLED".equals(l.getStatus()))
                .collect(Collectors.toList());
        dashboard.setPendingLabOrders(pendingLabs.size());
        dashboard.setPendingLabs(pendingLabs);

        return dashboard;
    }

    // ─────────────────────────────────────────────
    // ADMIN DASHBOARD
    // ─────────────────────────────────────────────
    public AdminDashboardResponse getAdminDashboard() {
        AdminDashboardResponse dashboard = new AdminDashboardResponse();

        // System-wide counts using .count() from JpaRepository
        // .count() runs: SELECT COUNT(*) FROM table_name
        dashboard.setTotalUsers(userRepository.count());
        dashboard.setTotalPatients(patientRepository.count());
        dashboard.setTotalDoctors(doctorRepository.count());
        dashboard.setTotalAppointments(appointmentRepository.count());
        dashboard.setTotalVisits(visitRepository.count());
        dashboard.setTotalPrescriptions(prescriptionRepository.count());
        dashboard.setTotalPharmacies(pharmacyRepository.count());

        // Lab order counts by status
        long totalLabs = labOrderRepository.count();
        long pendingLabs = labOrderRepository.findByStatus("ORDERED").size()
                + labOrderRepository.findByStatus("IN_PROGRESS").size();
        long completedLabs = labOrderRepository.findByStatus("COMPLETED").size();

        dashboard.setTotalLabOrders(totalLabs);
        dashboard.setPendingLabOrders(pendingLabs);
        dashboard.setCompletedLabOrders(completedLabs);

        return dashboard;
    }
}
