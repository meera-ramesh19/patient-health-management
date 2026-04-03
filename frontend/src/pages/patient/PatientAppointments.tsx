import { useState, useEffect } from 'react';
import { patientApi } from '../../api/patient';

export default function PatientAppointments() {
  const [appointments, setAppointments] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ doctorId: '', appointmentDate: '', reason: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const loadAppointments = () => {
    patientApi.getAppointments()
      .then(res => setAppointments(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadAppointments(); }, []);

  const handleBook = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await patientApi.bookAppointment({
        doctorId: Number(form.doctorId),
        appointmentDate: form.appointmentDate,
        reason: form.reason,
      });
      setSuccess('Appointment booked!');
      setShowForm(false);
      setForm({ doctorId: '', appointmentDate: '', reason: '' });
      loadAppointments();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to book appointment.');
    }
  };

  const handleCancel = async (id: number) => {
    if (!confirm('Cancel this appointment?')) return;
    try {
      await patientApi.cancelAppointment(id);
      loadAppointments();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to cancel.');
    }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>My Appointments</h1>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : 'Book Appointment'}
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      {showForm && (
        <form className="inline-form" onSubmit={handleBook}>
          <div className="form-group">
            <label>Doctor ID</label>
            <input type="number" value={form.doctorId} onChange={e => setForm({...form, doctorId: e.target.value})} required />
          </div>
          <div className="form-group">
            <label>Date & Time</label>
            <input type="datetime-local" value={form.appointmentDate} onChange={e => setForm({...form, appointmentDate: e.target.value})} required />
          </div>
          <div className="form-group">
            <label>Reason</label>
            <input type="text" value={form.reason} onChange={e => setForm({...form, reason: e.target.value})} required />
          </div>
          <button type="submit" className="btn btn-primary">Book</button>
        </form>
      )}

      <table className="data-table">
        <thead>
          <tr><th>Date</th><th>Reason</th><th>Status</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {appointments.map((appt: any) => (
            <tr key={appt.id}>
              <td>{appt.appointmentDate}</td>
              <td>{appt.reason}</td>
              <td><span className={`status status-${appt.status?.toLowerCase()}`}>{appt.status}</span></td>
              <td>
                {appt.status === 'SCHEDULED' && (
                  <button className="btn btn-danger btn-sm" onClick={() => handleCancel(appt.id)}>Cancel</button>
                )}
              </td>
            </tr>
          ))}
          {appointments.length === 0 && <tr><td colSpan={4}>No appointments found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
