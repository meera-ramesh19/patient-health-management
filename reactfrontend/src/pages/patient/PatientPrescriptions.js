import { useState, useEffect, useTransition } from 'react';
import { patientApi } from '../../api/patient';

export default function PatientPrescriptions() {
  const [prescriptions, setPrescriptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');
  const [isFiltering, startFilterTransition] = useTransition();

  const loadPrescriptions = (showActive) => {
    const request = showActive === 'active'
      ? patientApi.getActivePrescriptions()
      : patientApi.getPrescriptions();

    return request.then(res => setPrescriptions(res.data));
  };

  useEffect(() => {
    loadPrescriptions(filter)
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  // React 19: useTransition for non-blocking filter switch
  const handleFilterChange = (newFilter) => {
    setFilter(newFilter);
    startFilterTransition(async () => {
      try {
        await loadPrescriptions(newFilter);
      } catch (err) {
        console.error(err);
      }
    });
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>My Prescriptions</h1>
        <div className="filter-group">
          <button
            className={`btn btn-sm ${filter === 'all' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => handleFilterChange('all')}
            disabled={isFiltering}
          >All</button>
          <button
            className={`btn btn-sm ${filter === 'active' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => handleFilterChange('active')}
            disabled={isFiltering}
          >Active Only</button>
        </div>
      </div>
      {isFiltering && <div className="loading">Filtering...</div>}
      <table className="data-table">
        <thead>
          <tr><th>Medication</th><th>Dosage</th><th>Frequency</th><th>Start</th><th>End</th><th>Status</th></tr>
        </thead>
        <tbody>
          {prescriptions.map((rx) => (
            <tr key={rx.id}>
              <td>{rx.medicationName}</td>
              <td>{rx.dosage}</td>
              <td>{rx.frequency}</td>
              <td>{rx.startDate}</td>
              <td>{rx.endDate || '\u2014'}</td>
              <td><span className={`status status-${rx.active ? 'active' : 'inactive'}`}>{rx.active ? 'Active' : 'Inactive'}</span></td>
            </tr>
          ))}
          {prescriptions.length === 0 && <tr><td colSpan={6}>No prescriptions found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
