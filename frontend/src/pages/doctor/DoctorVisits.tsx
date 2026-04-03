import { useState, useEffect } from 'react';
import { doctorApi } from '../../api/doctor';

export default function DoctorVisits() {
  const [visits, setVisits] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState({ patientId: '', visitDate: '', diagnosis: '', notes: '' });
  const [editForm, setEditForm] = useState({ diagnosis: '', notes: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const loadVisits = () => {
    doctorApi.getVisits()
      .then(res => setVisits(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadVisits(); }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await doctorApi.recordVisit({
        patient: { id: Number(form.patientId) },
        visitDate: form.visitDate,
        diagnosis: form.diagnosis,
        notes: form.notes,
      });
      setSuccess('Visit recorded!');
      setShowForm(false);
      setForm({ patientId: '', visitDate: '', diagnosis: '', notes: '' });
      loadVisits();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to record visit.');
    }
  };

  const startEdit = (visit: any) => {
    setEditingId(visit.id);
    setEditForm({ diagnosis: visit.diagnosis || '', notes: visit.notes || '' });
  };

  const handleUpdate = async (id: number) => {
    setError('');
    setSuccess('');
    try {
      await doctorApi.updateVisit(id, {
        diagnosis: editForm.diagnosis,
        notes: editForm.notes,
      });
      setSuccess('Visit updated!');
      setEditingId(null);
      loadVisits();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update visit.');
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

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      {showForm && (
        <form className="inline-form" onSubmit={handleCreate}>
          <div className="form-group">
            <label>Patient ID</label>
            <input type="number" value={form.patientId} onChange={e => setForm({...form, patientId: e.target.value})} required />
          </div>
          <div className="form-group">
            <label>Visit Date</label>
            <input type="datetime-local" value={form.visitDate} onChange={e => setForm({...form, visitDate: e.target.value})} required />
          </div>
          <div className="form-group">
            <label>Diagnosis</label>
            <input type="text" value={form.diagnosis} onChange={e => setForm({...form, diagnosis: e.target.value})} required />
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
          <tr><th>Date</th><th>Patient</th><th>Diagnosis</th><th>Notes</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {visits.map((v: any) => (
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
