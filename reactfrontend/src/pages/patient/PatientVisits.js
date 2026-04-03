import { useState, useEffect } from 'react';
import { patientApi } from '../../api/patient';

export default function PatientVisits() {
  const [visits, setVisits] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    patientApi.getVisits()
      .then(res => setVisits(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <h1>My Visits</h1>
      <table className="data-table">
        <thead>
          <tr><th>Date</th><th>Diagnosis</th><th>Notes</th><th>Doctor</th></tr>
        </thead>
        <tbody>
          {visits.map((visit) => (
            <tr key={visit.id}>
              <td>{visit.visitDate}</td>
              <td>{visit.diagnosis}</td>
              <td>{visit.notes}</td>
              <td>Dr. {visit.doctor?.user?.username || visit.doctorId}</td>
            </tr>
          ))}
          {visits.length === 0 && <tr><td colSpan={4}>No visits recorded.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
