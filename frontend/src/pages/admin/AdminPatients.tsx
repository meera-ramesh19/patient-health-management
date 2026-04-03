import { useState, useEffect } from 'react';
import { adminApi } from '../../api/admin';

export default function AdminPatients() {
  const [patients, setPatients] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [searchName, setSearchName] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', phoneNumber: '', gender: '', bloodGroup: '' });

  const loadPatients = () => {
    setLoading(true);
    adminApi.getPatients()
      .then(res => setPatients(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadPatients(); }, []);

  const handleSearch = async () => {
    if (!searchName.trim()) { loadPatients(); return; }
    setLoading(true);
    try {
      const res = await adminApi.searchPatients(searchName);
      setPatients(res.data);
    } catch (err: any) { setError(err.response?.data?.message || 'Search failed.'); }
    finally { setLoading(false); }
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(''); setSuccess('');
    try {
      await adminApi.createPatient(form);
      setSuccess('Patient created!');
      setShowForm(false);
      setForm({ firstName: '', lastName: '', email: '', phoneNumber: '', gender: '', bloodGroup: '' });
      loadPatients();
    } catch (err: any) { setError(err.response?.data?.message || 'Create failed.'); }
  };

  const startEdit = (p: any) => {
    setEditingId(p.id);
    setForm({
      firstName: p.firstName || '', lastName: p.lastName || '', email: p.email || '',
      phoneNumber: p.phoneNumber || '', gender: p.gender || '', bloodGroup: p.bloodGroup || '',
    });
  };

  const handleUpdate = async (id: number) => {
    setError(''); setSuccess('');
    try {
      await adminApi.updatePatient(id, form);
      setSuccess('Patient updated!');
      setEditingId(null);
      loadPatients();
    } catch (err: any) { setError(err.response?.data?.message || 'Update failed.'); }
  };

  const handleAssignDoctor = async (patientId: number) => {
    const doctorId = prompt('Enter Doctor ID to assign:');
    if (!doctorId) return;
    try {
      await adminApi.assignDoctor(patientId, Number(doctorId));
      setSuccess('Doctor assigned!');
      loadPatients();
    } catch (err: any) { setError(err.response?.data?.message || 'Assign failed.'); }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this patient?')) return;
    try { await adminApi.deletePatient(id); setSuccess('Patient deleted.'); loadPatients(); }
    catch (err: any) { setError(err.response?.data?.message || 'Delete failed.'); }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>Patient Management</h1>
        <button className="btn btn-primary" onClick={() => { setShowForm(!showForm); setEditingId(null); }}>
          {showForm ? 'Cancel' : 'Add Patient'}
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      <div className="search-bar">
        <input type="text" placeholder="Search by last name..." value={searchName} onChange={e => setSearchName(e.target.value)} />
        <button className="btn btn-sm btn-primary" onClick={handleSearch}>Search</button>
        {searchName && <button className="btn btn-sm btn-secondary" onClick={() => { setSearchName(''); loadPatients(); }}>Clear</button>}
      </div>

      {(showForm || editingId !== null) && (
        <form className="inline-form" onSubmit={editingId ? (e) => { e.preventDefault(); handleUpdate(editingId); } : handleCreate}>
          <div className="form-row">
            <div className="form-group"><label>First Name</label><input type="text" value={form.firstName} onChange={e => setForm({...form, firstName: e.target.value})} required /></div>
            <div className="form-group"><label>Last Name</label><input type="text" value={form.lastName} onChange={e => setForm({...form, lastName: e.target.value})} required /></div>
          </div>
          <div className="form-row">
            <div className="form-group"><label>Email</label><input type="email" value={form.email} onChange={e => setForm({...form, email: e.target.value})} /></div>
            <div className="form-group"><label>Phone</label><input type="text" value={form.phoneNumber} onChange={e => setForm({...form, phoneNumber: e.target.value})} /></div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Gender</label>
              <select value={form.gender} onChange={e => setForm({...form, gender: e.target.value})}>
                <option value="">Select</option><option value="MALE">Male</option><option value="FEMALE">Female</option><option value="OTHER">Other</option>
              </select>
            </div>
            <div className="form-group"><label>Blood Group</label><input type="text" value={form.bloodGroup} onChange={e => setForm({...form, bloodGroup: e.target.value})} /></div>
          </div>
          <button type="submit" className="btn btn-primary">{editingId ? 'Update' : 'Create'}</button>
          {editingId && <button type="button" className="btn btn-secondary" onClick={() => setEditingId(null)} style={{marginLeft:8}}>Cancel</button>}
        </form>
      )}

      <table className="data-table">
        <thead>
          <tr><th>ID</th><th>Name</th><th>Email</th><th>Phone</th><th>Gender</th><th>Blood</th><th>Doctor</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {patients.map((p: any) => (
            <tr key={p.id}>
              <td>{p.id}</td>
              <td>{p.firstName} {p.lastName}</td>
              <td>{p.email || p.user?.email || '—'}</td>
              <td>{p.phoneNumber || '—'}</td>
              <td>{p.gender || '—'}</td>
              <td>{p.bloodGroup || '—'}</td>
              <td>{p.primaryDoctor?.name || p.primaryDoctor?.user?.username || '—'}</td>
              <td>
                <button className="btn btn-sm btn-secondary" onClick={() => startEdit(p)}>Edit</button>
                <button className="btn btn-sm btn-primary" onClick={() => handleAssignDoctor(p.id)}>Assign Dr</button>
                <button className="btn btn-sm btn-danger" onClick={() => handleDelete(p.id)}>Delete</button>
              </td>
            </tr>
          ))}
          {patients.length === 0 && <tr><td colSpan={8}>No patients found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
