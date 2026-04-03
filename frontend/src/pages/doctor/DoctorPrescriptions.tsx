import { useState, useEffect } from 'react';
import { doctorApi } from '../../api/doctor';

export default function DoctorPrescriptions() {
  const [prescriptions, setPrescriptions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    patientId: '', medicationName: '', dosage: '', frequency: '', startDate: '', endDate: '', notes: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const loadPrescriptions = () => {
    doctorApi.getPrescriptions()
      .then(res => setPrescriptions(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadPrescriptions(); }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await doctorApi.writePrescription({
        patientId: Number(form.patientId),
        medicationName: form.medicationName,
        dosage: form.dosage,
        frequency: form.frequency,
        startDate: form.startDate,
        endDate: form.endDate || undefined,
        notes: form.notes,
      });
      setSuccess('Prescription created!');
      setShowForm(false);
      setForm({ patientId: '', medicationName: '', dosage: '', frequency: '', startDate: '', endDate: '', notes: '' });
      loadPrescriptions();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create prescription.');
    }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>Prescriptions</h1>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : 'Write Prescription'}
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      {showForm && (
        <form className="inline-form" onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label>Patient ID</label>
              <input type="number" value={form.patientId} onChange={e => setForm({...form, patientId: e.target.value})} required />
            </div>
            <div className="form-group">
              <label>Medication</label>
              <input type="text" value={form.medicationName} onChange={e => setForm({...form, medicationName: e.target.value})} required />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Dosage</label>
              <input type="text" value={form.dosage} onChange={e => setForm({...form, dosage: e.target.value})} required />
            </div>
            <div className="form-group">
              <label>Frequency</label>
              <input type="text" value={form.frequency} onChange={e => setForm({...form, frequency: e.target.value})} required />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Start Date</label>
              <input type="date" value={form.startDate} onChange={e => setForm({...form, startDate: e.target.value})} required />
            </div>
            <div className="form-group">
              <label>End Date</label>
              <input type="date" value={form.endDate} onChange={e => setForm({...form, endDate: e.target.value})} />
            </div>
          </div>
          <div className="form-group">
            <label>Notes</label>
            <textarea value={form.notes} onChange={e => setForm({...form, notes: e.target.value})} />
          </div>
          <button type="submit" className="btn btn-primary">Save</button>
        </form>
      )}

      <table className="data-table">
        <thead>
          <tr><th>Medication</th><th>Patient</th><th>Dosage</th><th>Frequency</th><th>Start</th><th>End</th><th>Active</th></tr>
        </thead>
        <tbody>
          {prescriptions.map((rx: any) => (
            <tr key={rx.id}>
              <td>{rx.medicationName}</td>
              <td>{rx.patient?.user?.username || rx.patientId}</td>
              <td>{rx.dosage}</td>
              <td>{rx.frequency}</td>
              <td>{rx.startDate}</td>
              <td>{rx.endDate || '—'}</td>
              <td><span className={`status status-${rx.active ? 'active' : 'inactive'}`}>{rx.active ? 'Yes' : 'No'}</span></td>
            </tr>
          ))}
          {prescriptions.length === 0 && <tr><td colSpan={7}>No prescriptions found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
