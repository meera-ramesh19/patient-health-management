import { useState, useEffect } from 'react';
import { patientApi } from '../../api/patient';

export default function PatientPrescriptions() {
  const [prescriptions, setPrescriptions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'all' | 'active'>('all');

  const loadPrescriptions = (showActive: 'all' | 'active') => {
    setLoading(true);
    const request = showActive === 'active'
      ? patientApi.getActivePrescriptions()
      : patientApi.getPrescriptions();

    request
      .then(res => setPrescriptions(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadPrescriptions(filter); }, [filter]);

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>My Prescriptions</h1>
        <div className="filter-group">
          <button
            className={`btn btn-sm ${filter === 'all' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setFilter('all')}
          >All</button>
          <button
            className={`btn btn-sm ${filter === 'active' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setFilter('active')}
          >Active Only</button>
        </div>
      </div>
      <table className="data-table">
        <thead>
          <tr><th>Medication</th><th>Dosage</th><th>Frequency</th><th>Start</th><th>End</th><th>Status</th></tr>
        </thead>
        <tbody>
          {prescriptions.map((rx: any) => (
            <tr key={rx.id}>
              <td>{rx.medicationName}</td>
              <td>{rx.dosage}</td>
              <td>{rx.frequency}</td>
              <td>{rx.startDate}</td>
              <td>{rx.endDate || '—'}</td>
              <td><span className={`status status-${rx.active ? 'active' : 'inactive'}`}>{rx.active ? 'Active' : 'Inactive'}</span></td>
            </tr>
          ))}
          {prescriptions.length === 0 && <tr><td colSpan={6}>No prescriptions found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
