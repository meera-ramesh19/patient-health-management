import { useState, useEffect } from 'react';
import { adminApi } from '../../api/admin';

export default function AdminDoctors() {
  const [doctors, setDoctors] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [searchName, setSearchName] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState({ name: '', email: '', specialization: '', phoneNumber: '', hospitalAffiliation: '', licenseNumber: '' });

  const loadDoctors = () => {
    setLoading(true);
    adminApi.getDoctors()
      .then(res => setDoctors(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadDoctors(); }, []);

  const handleSearch = async () => {
    if (!searchName.trim()) { loadDoctors(); return; }
    setLoading(true);
    try { const res = await adminApi.searchDoctors(searchName); setDoctors(res.data); }
    catch (err: any) { setError(err.response?.data?.message || 'Search failed.'); }
    finally { setLoading(false); }
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(''); setSuccess('');
    try {
      await adminApi.createDoctor(form);
      setSuccess('Doctor created!');
      setShowForm(false);
      setForm({ name: '', email: '', specialization: '', phoneNumber: '', hospitalAffiliation: '', licenseNumber: '' });
      loadDoctors();
    } catch (err: any) { setError(err.response?.data?.message || 'Create failed.'); }
  };

  const startEdit = (d: any) => {
    setEditingId(d.id);
    setForm({
      name: d.name || '', email: d.email || d.user?.email || '',
      specialization: d.specialization || '', phoneNumber: d.phoneNumber || '',
      hospitalAffiliation: d.hospitalAffiliation || '', licenseNumber: d.licenseNumber || '',
    });
  };

  const handleUpdate = async (id: number) => {
    setError(''); setSuccess('');
    try {
      await adminApi.updateDoctor(id, form);
      setSuccess('Doctor updated!');
      setEditingId(null);
      loadDoctors();
    } catch (err: any) { setError(err.response?.data?.message || 'Update failed.'); }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this doctor?')) return;
    try { await adminApi.deleteDoctor(id); setSuccess('Doctor deleted.'); loadDoctors(); }
    catch (err: any) { setError(err.response?.data?.message || 'Delete failed.'); }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>Doctor Management</h1>
        <button className="btn btn-primary" onClick={() => { setShowForm(!showForm); setEditingId(null); }}>
          {showForm ? 'Cancel' : 'Add Doctor'}
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      <div className="search-bar">
        <input type="text" placeholder="Search by name..." value={searchName} onChange={e => setSearchName(e.target.value)} />
        <button className="btn btn-sm btn-primary" onClick={handleSearch}>Search</button>
        {searchName && <button className="btn btn-sm btn-secondary" onClick={() => { setSearchName(''); loadDoctors(); }}>Clear</button>}
      </div>

      {(showForm || editingId !== null) && (
        <form className="inline-form" onSubmit={editingId ? (e) => { e.preventDefault(); handleUpdate(editingId); } : handleCreate}>
          <div className="form-row">
            <div className="form-group"><label>Name</label><input type="text" value={form.name} onChange={e => setForm({...form, name: e.target.value})} required /></div>
            <div className="form-group"><label>Email</label><input type="email" value={form.email} onChange={e => setForm({...form, email: e.target.value})} /></div>
          </div>
          <div className="form-row">
            <div className="form-group"><label>Specialization</label><input type="text" value={form.specialization} onChange={e => setForm({...form, specialization: e.target.value})} /></div>
            <div className="form-group"><label>Phone</label><input type="text" value={form.phoneNumber} onChange={e => setForm({...form, phoneNumber: e.target.value})} /></div>
          </div>
          <div className="form-row">
            <div className="form-group"><label>Hospital</label><input type="text" value={form.hospitalAffiliation} onChange={e => setForm({...form, hospitalAffiliation: e.target.value})} /></div>
            <div className="form-group"><label>License #</label><input type="text" value={form.licenseNumber} onChange={e => setForm({...form, licenseNumber: e.target.value})} /></div>
          </div>
          <button type="submit" className="btn btn-primary">{editingId ? 'Update' : 'Create'}</button>
          {editingId && <button type="button" className="btn btn-secondary" onClick={() => setEditingId(null)} style={{marginLeft:8}}>Cancel</button>}
        </form>
      )}

      <table className="data-table">
        <thead>
          <tr><th>ID</th><th>Name</th><th>Email</th><th>Specialization</th><th>Phone</th><th>License</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {doctors.map((d: any) => (
            <tr key={d.id}>
              <td>{d.id}</td>
              <td>{d.name || d.user?.username || '—'}</td>
              <td>{d.email || d.user?.email || '—'}</td>
              <td>{d.specialization || '—'}</td>
              <td>{d.phoneNumber || '—'}</td>
              <td>{d.licenseNumber || '—'}</td>
              <td>
                <button className="btn btn-sm btn-secondary" onClick={() => startEdit(d)}>Edit</button>
                <button className="btn btn-sm btn-danger" onClick={() => handleDelete(d.id)}>Delete</button>
              </td>
            </tr>
          ))}
          {doctors.length === 0 && <tr><td colSpan={7}>No doctors found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
