import { useState, useEffect } from 'react';
import { patientApi } from '../../api/patient';

export default function PatientProfile() {
  const [profile, setProfile] = useState<any>(null);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({ phoneNumber: '', address: '', dateOfBirth: '', gender: '', bloodGroup: '' });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    patientApi.getProfile()
      .then(res => {
        setProfile(res.data);
        setForm({
          phoneNumber: res.data.phoneNumber || '',
          address: res.data.address || '',
          dateOfBirth: res.data.dateOfBirth || '',
          gender: res.data.gender || '',
          bloodGroup: res.data.bloodGroup || '',
        });
      })
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const res = await patientApi.updateProfile(form);
      setProfile(res.data);
      setEditing(false);
      setSuccess('Profile updated!');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Update failed.');
    }
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (!profile) return <div className="error-message">Failed to load profile.</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>My Profile</h1>
        {!editing && <button className="btn btn-primary" onClick={() => setEditing(true)}>Edit</button>}
      </div>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      {editing ? (
        <form className="profile-form" onSubmit={handleSave}>
          <div className="form-group">
            <label>Phone</label>
            <input type="text" value={form.phoneNumber} onChange={e => setForm({...form, phoneNumber: e.target.value})} />
          </div>
          <div className="form-group">
            <label>Address</label>
            <input type="text" value={form.address} onChange={e => setForm({...form, address: e.target.value})} />
          </div>
          <div className="form-group">
            <label>Date of Birth</label>
            <input type="date" value={form.dateOfBirth} onChange={e => setForm({...form, dateOfBirth: e.target.value})} />
          </div>
          <div className="form-group">
            <label>Gender</label>
            <select value={form.gender} onChange={e => setForm({...form, gender: e.target.value})}>
              <option value="">Select</option>
              <option value="MALE">Male</option>
              <option value="FEMALE">Female</option>
              <option value="OTHER">Other</option>
            </select>
          </div>
          <div className="form-group">
            <label>Blood Group</label>
            <input type="text" value={form.bloodGroup} onChange={e => setForm({...form, bloodGroup: e.target.value})} />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">Save</button>
            <button type="button" className="btn btn-secondary" onClick={() => setEditing(false)}>Cancel</button>
          </div>
        </form>
      ) : (
        <div className="profile-details">
          <p><strong>Name:</strong> {profile.user?.username}</p>
          <p><strong>Email:</strong> {profile.user?.email}</p>
          <p><strong>Phone:</strong> {profile.phoneNumber || '—'}</p>
          <p><strong>Address:</strong> {profile.address || '—'}</p>
          <p><strong>Date of Birth:</strong> {profile.dateOfBirth || '—'}</p>
          <p><strong>Gender:</strong> {profile.gender || '—'}</p>
          <p><strong>Blood Group:</strong> {profile.bloodGroup || '—'}</p>
        </div>
      )}
    </div>
  );
}
