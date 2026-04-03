import { useState, useEffect, useActionState } from 'react';
import { doctorApi } from '../../api/doctor';

export default function DoctorVisits() {
  const [visits, setVisits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [editForm, setEditForm] = useState({ diagnosis: '', notes: '' });

  const loadVisits = () => {
    doctorApi.getVisits()
      .then(res => setVisits(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadVisits(); }, []);

  // React 19: useActionState for create form
  const [createState, createAction, isCreating] = useActionState(async (_prev, formData) => {
    try {
      await doctorApi.recordVisit({
        patient: { id: Number(formData.get('patientId')) },
        visitDate: formData.get('visitDate'),
        diagnosis: formData.get('diagnosis'),
        notes: formData.get('notes'),
      });
      setShowForm(false);
      loadVisits();
      return { error: null, success: 'Visit recorded!' };
    } catch (err) {
      return { error: err.response?.data?.message || 'Failed to record visit.', success: null };
    }
  }, { error: null, success: null });

  const startEdit = (visit) => {
    setEditingId(visit.id);
    setEditForm({ diagnosis: visit.diagnosis || '', notes: visit.notes || '' });
  };

  const handleUpdate = async (id) => {
    try {
      await doctorApi.updateVisit(id, {
        diagnosis: editForm.diagnosis,
        notes: editForm.notes,
      });
      setEditingId(null);
      loadVisits();
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>Visits</h1>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : 'Record Visit'}
        </button>
      </div>

      {createState.error && <div className="error-message">{createState.error}</div>}
      {createState.success && <div className="success-message">{createState.success}</div>}

      {showForm && (
        <form className="inline-form" action={createAction}>
          <div className="form-group">
            <label>Patient ID</label>
            <input type="number" name="patientId" required />
          </div>
          <div className="form-group">
            <label>Visit Date</label>
            <input type="datetime-local" name="visitDate" required />
          </div>
          <div className="form-group">
            <label>Diagnosis</label>
            <input type="text" name="diagnosis" required />
          </div>
          <div className="form-group">
            <label>Notes</label>
            <textarea name="notes" />
          </div>
          <button type="submit" className="btn btn-primary" disabled={isCreating}>
            {isCreating ? 'Saving...' : 'Save'}
          </button>
        </form>
      )}

      <table className="data-table">
        <thead>
          <tr><th>Date</th><th>Patient</th><th>Diagnosis</th><th>Notes</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {visits.map((v) => (
            <tr key={v.id}>
              <td>{v.visitDate}</td>
              <td>{v.patient?.user?.username || v.patient?.firstName || v.patientId}</td>
              {editingId === v.id ? (
                <>
                  <td><input type="text" value={editForm.diagnosis} onChange={e => setEditForm({...editForm, diagnosis: e.target.value})} className="inline-input" /></td>
                  <td><input type="text" value={editForm.notes} onChange={e => setEditForm({...editForm, notes: e.target.value})} className="inline-input" /></td>
                  <td>
                    <button className="btn btn-sm btn-primary" onClick={() => handleUpdate(v.id)}>Save</button>
                    <button className="btn btn-sm btn-secondary" onClick={() => setEditingId(null)}>Cancel</button>
                  </td>
                </>
              ) : (
                <>
                  <td>{v.diagnosis}</td>
                  <td>{v.notes}</td>
                  <td>
                    <button className="btn btn-sm btn-secondary" onClick={() => startEdit(v)}>Edit</button>
                  </td>
                </>
              )}
            </tr>
          ))}
          {visits.length === 0 && <tr><td colSpan={5}>No visits found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
