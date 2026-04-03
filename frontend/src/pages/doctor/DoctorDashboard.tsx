import { useState, useEffect } from 'react';
import { doctorApi } from '../../api/doctor';

export default function DoctorDashboard() {
  const [dashboard, setDashboard] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    doctorApi.getDashboard()
      .then(res => setDashboard(res.data))
      .catch(err => console.error('Failed to load dashboard:', err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading">Loading dashboard...</div>;
  if (!dashboard) return <div className="error-message">Failed to load dashboard.</div>;

  return (
    <div className="page">
      <h1>Doctor Dashboard</h1>
      <div className="dashboard-cards">
        <div className="card">
          <h3>Total Patients</h3>
          <span className="card-number">{dashboard.totalPatients}</span>
        </div>
        <div className="card">
          <h3>Upcoming Appointments</h3>
          <span className="card-number">{dashboard.upcomingAppointments}</span>
        </div>
        <div className="card">
          <h3>Pending Lab Orders</h3>
          <span className="card-number">{dashboard.pendingLabOrders}</span>
        </div>
      </div>

      {dashboard.todaysAppointments?.length > 0 && (
        <div className="section">
          <h2>Today's Appointments</h2>
          <table className="data-table">
            <thead>
              <tr><th>Time</th><th>Patient</th><th>Reason</th><th>Status</th></tr>
            </thead>
            <tbody>
              {dashboard.todaysAppointments.map((appt: any) => (
                <tr key={appt.id}>
                  <td>{appt.appointmentDate}</td>
                  <td>{appt.patient?.user?.username || appt.patientId}</td>
                  <td>{appt.reason}</td>
                  <td><span className={`status status-${appt.status?.toLowerCase()}`}>{appt.status}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {dashboard.pendingLabs?.length > 0 && (
        <div className="section">
          <h2>Pending Lab Results</h2>
          <table className="data-table">
            <thead>
              <tr><th>Test</th><th>Patient</th><th>Ordered</th><th>Status</th></tr>
            </thead>
            <tbody>
              {dashboard.pendingLabs.map((lab: any) => (
                <tr key={lab.id}>
                  <td>{lab.testName}</td>
                  <td>{lab.patient?.user?.username || lab.patientId}</td>
                  <td>{lab.orderDate}</td>
                  <td><span className={`status status-${lab.status?.toLowerCase()}`}>{lab.status}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
