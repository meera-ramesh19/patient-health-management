import { useState, useEffect, useOptimistic } from 'react';
import { adminApi } from '../../api/admin';

export default function AdminAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [optimisticAppointments, updateOptimistic] = useOptimistic(
    appointments,
    (current, action) => {
      if (action.type === 'cancel') {
        return current.map(a => a.id === action.id ? { ...a, status: 'CANCELLED' } : a);
      }
      if (action.type === 'delete') {
        return current.filter(a => a.id !== action.id);
      }
      return current;
    }
  );

  const loadAppointments = () => {
    setLoading(true);
    adminApi.getAppointments()
      .then(res => setAppointments(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadAppointments(); }, []);

  const handleCancel = async (id) => {
    if (!window.confirm('Cancel this appointment?')) return;
    updateOptimistic({ type: 'cancel', id });
    try { await adminApi.cancelAppointment(id); setSuccess('Appointment cancelled.'); loadAppointments(); }
    catch (err) { setError(err.response?.data?.message || 'Cancel failed.'); loadAppointments(); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Permanently delete this appointment?')) return;
    updateOptimistic({ type: 'delete', id });
    try { await adminApi.deleteAppointment(id); setSuccess('Appointment deleted.'); loadAppointments(); }
    catch (err) { setError(err.response?.data?.message || 'Delete failed.'); loadAppointments(); }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <h1>All Appointments</h1>
      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      <table className="data-table">
        <thead>
          <tr><th>ID</th><th>Date</th><th>Patient</th><th>Doctor</th><th>Reason</th><th>Status</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {optimisticAppointments.map((appt) => (
            <tr key={appt.id}>
              <td>{appt.id}</td>
              <td>{appt.dateTime || appt.appointmentDate}</td>
              <td>{appt.patient?.user?.username || appt.patient?.firstName || '\u2014'}</td>
              <td>{appt.doctor?.name || appt.doctor?.user?.username || '\u2014'}</td>
              <td>{appt.reason}</td>
              <td><span className={`status status-${appt.status?.toLowerCase()}`}>{appt.status}</span></td>
              <td>
                {appt.status === 'SCHEDULED' && (
                  <button className="btn btn-sm btn-warning" onClick={() => handleCancel(appt.id)}>Cancel</button>
                )}
                <button className="btn btn-sm btn-danger" onClick={() => handleDelete(appt.id)}>Delete</button>
              </td>
            </tr>
          ))}
          {optimisticAppointments.length === 0 && <tr><td colSpan={7}>No appointments found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
