package com.labservice.dto.response;

import com.labservice.model.Appointment;
import com.labservice.model.LabOrder;
import java.util.List;

// Dashboard data for the Doctor Portal home page.
//
// When a doctor logs in, the dashboard shows:
//   - Total number of assigned patients
//   - How many appointments they have today/upcoming
//   - How many lab orders are pending results
//   - Their upcoming appointments list
//   - Lab orders still waiting for results

public class DoctorDashboardResponse {

    private int totalPatients;
    private int upcomingAppointments;
    private int pendingLabOrders;
    private List<Appointment> todaysAppointments;
    private List<LabOrder> pendingLabs;

    public DoctorDashboardResponse() {}

    public int getTotalPatients() { return totalPatients; }
    public void setTotalPatients(int totalPatients) { this.totalPatients = totalPatients; }

    public int getUpcomingAppointments() { return upcomingAppointments; }
    public void setUpcomingAppointments(int upcomingAppointments) { this.upcomingAppointments = upcomingAppointments; }

    public int getPendingLabOrders() { return pendingLabOrders; }
    public void setPendingLabOrders(int pendingLabOrders) { this.pendingLabOrders = pendingLabOrders; }

    public List<Appointment> getTodaysAppointments() { return todaysAppointments; }
    public void setTodaysAppointments(List<Appointment> todaysAppointments) { this.todaysAppointments = todaysAppointments; }

    public List<LabOrder> getPendingLabs() { return pendingLabs; }
    public void setPendingLabs(List<LabOrder> pendingLabs) { this.pendingLabs = pendingLabs; }
}
