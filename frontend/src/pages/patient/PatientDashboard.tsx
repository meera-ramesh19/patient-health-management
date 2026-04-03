import { useState, useEffect } from 'react';
import { patientApi } from '../../api/patient';

export default function PatientDashboard() {
  const [dashboard, setDashboard] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    patientApi.getDashboard()
      .then(res => setDashboard(res.data))
      .catch(err => console.error('Failed to load dashboard:', err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading">Loading dashboard...</div>;
  if (!dashboard) return <div className="error-message">Failed to load dashboard.</div>;

  return (
    <div className="page">
      <h1>Patient Dashboard</h1>
      <div className="dashboard-cards">
        <div className="card">
          <h3>Upcoming Appointments</h3>
          <span className="card-number">{dashboard.upcomingAppointments}</span>
        </div>
        <div className="card">
          <h3>Active Prescriptions</h3>
          <span className="card-number">{dashboard.activePrescriptions}</span>
        </div>
        <div className="card">
          <h3>Pending Lab Orders</h3>
          <span className="card-number">{dashboard.pendingLabOrders}</span>
        </div>
      </div>

      {dashboard.nextAppointment && (
        <div className="section">
          <h2>Next Appointment</h2>
          <p><strong>Date:</strong> {dashboard.nextAppointment.appointmentDate}</p>
          <p><strong>Reason:</strong> {dashboard.nextAppointment.reason}</p>
          <p><strong>Status:</strong> {dashboard.nextAppointment.status}</p>
        </div>
      )}

      {dashboard.currentMedications?.length > 0 && (
        <div className="section">
          <h2>Current Medications</h2>
          <table className="data-table">
            <thead>
              <tr><th>Medication</th><th>Dosage</th><th>Frequency</th></tr>
            </thead>
            <tbody>
              {dashboard.currentMedications.map((med: any) => (
                <tr key={med.id}>
                  <td>{med.medicationName}</td>
                  <td>{med.dosage}</td>
                  <td>{med.frequency}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {dashboard.recentLabOrders?.length > 0 && (
        <div className="section">
          <h2>Recent Lab Orders</h2>
          <table className="data-table">
            <thead>
              <tr><th>Test</th><th>Status</th><th>Ordered</th></tr>
            </thead>
            <tbody>
              {dashboard.recentLabOrders.map((lab: any) => (
                <tr key={lab.id}>
                  <td>{lab.testName}</td>
                  <td><span className={`status status-${lab.status?.toLowerCase()}`}>{lab.status}</span></td>
                  <td>{lab.orderDate}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
