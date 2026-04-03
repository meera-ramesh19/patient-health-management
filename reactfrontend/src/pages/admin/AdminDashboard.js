import { useState, useEffect } from 'react';
import { adminApi } from '../../api/admin';

export default function AdminDashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminApi.getDashboard()
      .then(res => setDashboard(res.data))
      .catch(err => console.error('Failed to load dashboard:', err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading">Loading dashboard...</div>;
  if (!dashboard) return <div className="error-message">Failed to load dashboard.</div>;

  return (
    <div className="page">
      <h1>Admin Dashboard</h1>
      <div className="dashboard-cards">
        <div className="card">
          <h3>Total Users</h3>
          <span className="card-number">{dashboard.totalUsers}</span>
        </div>
        <div className="card">
          <h3>Patients</h3>
          <span className="card-number">{dashboard.totalPatients}</span>
        </div>
        <div className="card">
          <h3>Doctors</h3>
          <span className="card-number">{dashboard.totalDoctors}</span>
        </div>
        <div className="card">
          <h3>Appointments</h3>
          <span className="card-number">{dashboard.totalAppointments}</span>
        </div>
        <div className="card">
          <h3>Visits</h3>
          <span className="card-number">{dashboard.totalVisits}</span>
        </div>
        <div className="card">
          <h3>Lab Orders</h3>
          <span className="card-number">{dashboard.totalLabOrders}</span>
        </div>
        <div className="card">
          <h3>Pending Labs</h3>
          <span className="card-number">{dashboard.pendingLabOrders}</span>
        </div>
        <div className="card">
          <h3>Completed Labs</h3>
          <span className="card-number">{dashboard.completedLabOrders}</span>
        </div>
        <div className="card">
          <h3>Prescriptions</h3>
          <span className="card-number">{dashboard.totalPrescriptions}</span>
        </div>
        <div className="card">
          <h3>Pharmacies</h3>
          <span className="card-number">{dashboard.totalPharmacies}</span>
        </div>
      </div>
    </div>
  );
}
