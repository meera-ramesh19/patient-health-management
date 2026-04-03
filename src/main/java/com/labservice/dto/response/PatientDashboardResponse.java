package com.labservice.dto.response;

import com.labservice.model.Appointment;
import com.labservice.model.LabOrder;
import com.labservice.model.Prescription;
import java.util.List;

// Dashboard data for the Patient Portal home page.
//
// When a patient logs in, the dashboard shows:
//   - How many upcoming appointments they have
//   - How many active prescriptions
//   - How many pending lab results
//   - Their next upcoming appointment
//   - Their currently active medications
//   - Their recent lab orders
//
// This is a DTO (Data Transfer Object) — it shapes the JSON
// response without exposing raw entities.

public class PatientDashboardResponse {

    private int upcomingAppointments;
    private int activePrescriptions;
    private int pendingLabOrders;
    private Appointment nextAppointment;
    private List<Prescription> currentMedications;
    private List<LabOrder> recentLabOrders;

    public PatientDashboardResponse() {}

    // Getters and setters
    public int getUpcomingAppointments() { return upcomingAppointments; }
    public void setUpcomingAppointments(int upcomingAppointments) { this.upcomingAppointments = upcomingAppointments; }

    public int getActivePrescriptions() { return activePrescriptions; }
    public void setActivePrescriptions(int activePrescriptions) { this.activePrescriptions = activePrescriptions; }

    public int getPendingLabOrders() { return pendingLabOrders; }
    public void setPendingLabOrders(int pendingLabOrders) { this.pendingLabOrders = pendingLabOrders; }

    public Appointment getNextAppointment() { return nextAppointment; }
    public void setNextAppointment(Appointment nextAppointment) { this.nextAppointment = nextAppointment; }

    public List<Prescription> getCurrentMedications() { return currentMedications; }
    public void setCurrentMedications(List<Prescription> currentMedications) { this.currentMedications = currentMedications; }

    public List<LabOrder> getRecentLabOrders() { return recentLabOrders; }
    public void setRecentLabOrders(List<LabOrder> recentLabOrders) { this.recentLabOrders = recentLabOrders; }
}
