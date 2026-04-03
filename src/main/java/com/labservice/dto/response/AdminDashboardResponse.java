package com.labservice.dto.response;

// Dashboard data for the Admin Portal home page.
//
// When an admin logs in, the dashboard shows system-wide stats:
//   - Total counts of users, patients, doctors
//   - Appointment and visit counts
//   - Lab order counts by status
//   - Prescription count

public class AdminDashboardResponse {

    private long totalUsers;
    private long totalPatients;
    private long totalDoctors;
    private long totalAppointments;
    private long totalVisits;
    private long totalLabOrders;
    private long pendingLabOrders;
    private long completedLabOrders;
    private long totalPrescriptions;
    private long totalPharmacies;

    public AdminDashboardResponse() {}

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalPatients() { return totalPatients; }
    public void setTotalPatients(long totalPatients) { this.totalPatients = totalPatients; }

    public long getTotalDoctors() { return totalDoctors; }
    public void setTotalDoctors(long totalDoctors) { this.totalDoctors = totalDoctors; }

    public long getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(long totalAppointments) { this.totalAppointments = totalAppointments; }

    public long getTotalVisits() { return totalVisits; }
    public void setTotalVisits(long totalVisits) { this.totalVisits = totalVisits; }

    public long getTotalLabOrders() { return totalLabOrders; }
    public void setTotalLabOrders(long totalLabOrders) { this.totalLabOrders = totalLabOrders; }

    public long getPendingLabOrders() { return pendingLabOrders; }
    public void setPendingLabOrders(long pendingLabOrders) { this.pendingLabOrders = pendingLabOrders; }

    public long getCompletedLabOrders() { return completedLabOrders; }
    public void setCompletedLabOrders(long completedLabOrders) { this.completedLabOrders = completedLabOrders; }

    public long getTotalPrescriptions() { return totalPrescriptions; }
    public void setTotalPrescriptions(long totalPrescriptions) { this.totalPrescriptions = totalPrescriptions; }

    public long getTotalPharmacies() { return totalPharmacies; }
    public void setTotalPharmacies(long totalPharmacies) { this.totalPharmacies = totalPharmacies; }
}
