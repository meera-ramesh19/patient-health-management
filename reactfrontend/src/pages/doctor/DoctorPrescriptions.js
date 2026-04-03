import { useState, useEffect, useActionState } from 'react';
import { doctorApi } from '../../api/doctor';

export default function DoctorPrescriptions() {
  const [prescriptions, setPrescriptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);

  const loadPrescriptions = () => {
    doctorApi.getPrescriptions()
      .then(res => setPrescriptions(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadPrescriptions(); }, []);

  // React 19: useActionState for prescription form
  const [state, submitAction, isPending] = useActionState(async (_prev, formData) => {
    try {
      await doctorApi.writePrescription({
        patientId: Number(formData.get('patientId')),
        medicationName: formData.get('medicationName'),
        dosage: formData.get('dosage'),
        frequency: formData.get('frequency'),
        startDate: formData.get('startDate'),
        endDate: formData.get('endDate') || undefined,
        notes: formData.get('notes'),
      });
      setShowForm(false);
      loadPrescriptions();
      return { error: null, success: 'Prescription created!' };
    } catch (err) {
      return { error: err.response?.data?.message || 'Failed to create prescription.', success: null };
    }
  }, { error: null, success: null });

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>Prescriptions</h1>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : 'Write Prescription'}
        </button>
      </div>

      {state.error && <div className="error-message">{state.error}</div>}
      {state.success && <div className="success-message">{state.success}</div>}

      {showForm && (
        <form className="inline-form" action={submitAction}>
          <div className="form-row">
            <div className="form-group">
              <label>Patient ID</label>
              <input type="number" name="patientId" required />
            </div>
            <div className="form-group">
              <label>Medication</label>
              <input type="text" name="medicationName" required />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Dosage</label>
              <input type="text" name="dosage" required />
            </div>
            <div className="form-group">
              <label>Frequency</label>
              <input type="text" name="frequency" required />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Start Date</label>
              <input type="date" name="startDate" required />
            </div>
            <div className="form-group">
              <label>End Date</label>
              <input type="date" name="endDate" />
            </div>
          </div>
          <div className="form-group">
            <label>Notes</label>
            <textarea name="notes" />
          </div>
          <button type="submit" className="btn btn-primary" disabled={isPending}>
            {isPending ? 'Saving...' : 'Save'}
          </button>
        </form>
      )}

      <table className="data-table">
        <thead>
          <tr><th>Medication</th><th>Patient</th><th>Dosage</th><th>Frequency</th><th>Start</th><th>End</th><th>Active</th></tr>
        </thead>
        <tbody>
          {prescriptions.map((rx) => (
            <tr key={rx.id}>
              <td>{rx.medicationName}</td>
              <td>{rx.patient?.user?.username || rx.patientId}</td>
              <td>{rx.dosage}</td>
              <td>{rx.frequency}</td>
              <td>{rx.startDate}</td>
              <td>{rx.endDate || '\u2014'}</td>
              <td><span className={`status status-${rx.active ? 'active' : 'inactive'}`}>{rx.active ? 'Yes' : 'No'}</span></td>
            </tr>
          ))}
          {prescriptions.length === 0 && <tr><td colSpan={7}>No prescriptions found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
