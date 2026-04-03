import { useState, useEffect } from 'react';
import { doctorApi } from '../../api/doctor';

export default function DoctorAppointments() {
  const [appointments, setAppointments] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    doctorApi.getAppointments()
      .then(res => setAppointments(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <h1>Appointments</h1>
      <table className="data-table">
        <thead>
          <tr><th>Date</th><th>Patient</th><th>Reason</th><th>Status</th></tr>
        </thead>
        <tbody>
          {appointments.map((appt: any) => (
            <tr key={appt.id}>
              <td>{appt.appointmentDate}</td>
              <td>{appt.patient?.user?.username || appt.patientId}</td>
              <td>{appt.reason}</td>
              <td><span className={`status status-${appt.status?.toLowerCase()}`}>{appt.status}</span></td>
            </tr>
          ))}
          {appointments.length === 0 && <tr><td colSpan={4}>No appointments found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
