import { useState, useEffect, useOptimistic, useTransition } from 'react';
import { adminApi } from '../../api/admin';

export default function AdminPharmacies() {
  const [pharmacies, setPharmacies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [searchName, setSearchName] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState({ name: '', phoneNumber: '', email: '', licenseNumber: '' });
  const [isSearching, startSearchTransition] = useTransition();

  const [optimisticPharmacies, removeOptimistic] = useOptimistic(
    pharmacies,
    (current, deletedId) => current.filter(p => p.id !== deletedId)
  );

  const loadPharmacies = () => {
    setLoading(true);
    adminApi.getPharmacies()
      .then(res => setPharmacies(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadPharmacies(); }, []);

  const handleSearch = () => {
    if (!searchName.trim()) { loadPharmacies(); return; }
    startSearchTransition(async () => {
      try { const res = await adminApi.searchPharmacies(searchName); setPharmacies(res.data); }
      catch (err) { setError(err.response?.data?.message || 'Search failed.'); }
    });
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');
    try {
      await adminApi.createPharmacy(form);
      setSuccess('Pharmacy created!');
      setShowForm(false);
      setForm({ name: '', phoneNumber: '', email: '', licenseNumber: '' });
      loadPharmacies();
    } catch (err) { setError(err.response?.data?.message || 'Create failed.'); }
  };

  const startEdit = (p) => {
    setEditingId(p.id);
    setForm({
      name: p.name || '', phoneNumber: p.phoneNumber || '',
      email: p.email || '', licenseNumber: p.licenseNumber || '',
    });
  };

  const handleUpdate = async (id) => {
    setError(''); setSuccess('');
    try {
      await adminApi.updatePharmacy(id, form);
      setSuccess('Pharmacy updated!');
      setEditingId(null);
      loadPharmacies();
    } catch (err) { setError(err.response?.data?.message || 'Update failed.'); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this pharmacy?')) return;
    removeOptimistic(id);
    try { await adminApi.deletePharmacy(id); setSuccess('Pharmacy deleted.'); loadPharmacies(); }
    catch (err) { setError(err.response?.data?.message || 'Delete failed.'); loadPharmacies(); }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>Pharmacy Management</h1>
        <button className="btn btn-primary" onClick={() => { setShowForm(!showForm); setEditingId(null); }}>
          {showForm ? 'Cancel' : 'Add Pharmacy'}
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      <div className="search-bar">
        <input type="text" placeholder="Search by name..." value={searchName} onChange={e => setSearchName(e.target.value)} />
        <button className="btn btn-sm btn-primary" onClick={handleSearch} disabled={isSearching}>
          {isSearching ? 'Searching...' : 'Search'}
        </button>
        {searchName && <button className="btn btn-sm btn-secondary" onClick={() => { setSearchName(''); loadPharmacies(); }}>Clear</button>}
      </div>

      {(showForm || editingId !== null) && (
        <form className="inline-form" onSubmit={editingId ? (e) => { e.preventDefault(); handleUpdate(editingId); } : handleCreate}>
          <div className="form-row">
            <div className="form-group"><label>Name</label><input type="text" value={form.name} onChange={e => setForm({...form, name: e.target.value})} required /></div>
            <div className="form-group"><label>Email</label><input type="email" value={form.email} onChange={e => setForm({...form, email: e.target.value})} /></div>
          </div>
          <div className="form-row">
            <div className="form-group"><label>Phone</label><input type="text" value={form.phoneNumber} onChange={e => setForm({...form, phoneNumber: e.target.value})} /></div>
            <div className="form-group"><label>License #</label><input type="text" value={form.licenseNumber} onChange={e => setForm({...form, licenseNumber: e.target.value})} /></div>
          </div>
          <button type="submit" className="btn btn-primary">{editingId ? 'Update' : 'Create'}</button>
          {editingId && <button type="button" className="btn btn-secondary" onClick={() => setEditingId(null)} style={{marginLeft:8}}>Cancel</button>}
        </form>
      )}

      <table className="data-table">
        <thead>
          <tr><th>ID</th><th>Name</th><th>Phone</th><th>Email</th><th>License</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {optimisticPharmacies.map((p) => (
            <tr key={p.id}>
              <td>{p.id}</td>
              <td>{p.name}</td>
              <td>{p.phoneNumber || '\u2014'}</td>
              <td>{p.email || '\u2014'}</td>
              <td>{p.licenseNumber || '\u2014'}</td>
              <td>
                <button className="btn btn-sm btn-secondary" onClick={() => startEdit(p)}>Edit</button>
                <button className="btn btn-sm btn-danger" onClick={() => handleDelete(p.id)}>Delete</button>
              </td>
            </tr>
          ))}
          {optimisticPharmacies.length === 0 && <tr><td colSpan={6}>No pharmacies found.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
