import { useState, useEffect, useActionState, useOptimistic } from 'react';
import { patientApi } from '../../api/patient';

export default function PatientAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);

  const [optimisticAppointments, removeOptimistic] = useOptimistic(
    appointments,
    (current, cancelledId) => current.map(a =>
      a.id === cancelledId ? { ...a, status: 'CANCELLED' } : a
    )
  );

  const loadAppointments = () => {
    patientApi.getAppointments()
      .then(res => setAppointments(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadAppointments(); }, []);

  // React 19: useActionState for booking form
  const [bookState, bookAction, isBooking] = useActionState(async (_prev, formData) => {
    try {
      await patientApi.bookAppointment({
        doctorId: Number(formData.get('doctorId')),
        appointmentDate: formData.get('appointmentDate'),
        reason: formData.get('reason'),
      });
      setShowForm(false);
      loadAppointments();
      return { error: null, success: 'Appointment booked!' };
    } catch (err) {
      return { error: err.response?.data?.message || 'Failed to book appointment.', success: null };
    }
  }, { error: null, success: null });

  const handleCancel = async (id) => {
    if (!window.confirm('Cancel this appointment?')) return;
    removeOptimistic(id);
    try {
      await patientApi.cancelAppointment(id);
      loadAppointments();
    } catch (err) {
      loadAppointments();
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

      {bookState.error && <div className="error-message">{bookState.error}</div>}
      {bookState.success && <div className="success-message">{bookState.success}</div>}

      {showForm && (
        <form className="inline-form" action={bookAction}>
          <div className="form-group">
            <label>Doctor ID</label>
            <input type="number" name="doctorId" required />
          </div>
          <div className="form-group">
            <label>Date & Time</label>
            <input type="datetime-local" name="appointmentDate" required />
          </div>
          <div className="form-group">
            <label>Reason</label>
            <input type="text" name="reason" required />
          </div>
          <button type="submit" className="btn btn-primary" disabled={isBooking}>
            {isBooking ? 'Booking...' : 'Book'}
          </button>
        </form>
      )}

      <table className="data-table">
        <thead>
          <tr><th>Date</th><th>Reason</th><th>Status</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {optimisticAppointments.map((appt) => (
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
          {optimisticAppointments.length === 0 && <tr><td colSpan={4}>No appointments found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
