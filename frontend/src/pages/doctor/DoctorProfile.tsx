import { useState, useEffect } from 'react';
import { doctorApi } from '../../api/doctor';

export default function DoctorProfile() {
  const [profile, setProfile] = useState<any>(null);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({
    specialization: '', phoneNumber: '', hospitalAffiliation: '',
    yearsOfExperience: '', consultationFee: ''
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    doctorApi.getProfile()
      .then(res => {
        setProfile(res.data);
        setForm({
          specialization: res.data.specialization || '',
          phoneNumber: res.data.phoneNumber || '',
          hospitalAffiliation: res.data.hospitalAffiliation || '',
          yearsOfExperience: res.data.yearsOfExperience?.toString() || '',
          consultationFee: res.data.consultationFee?.toString() || '',
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
      const res = await doctorApi.updateProfile({
        specialization: form.specialization,
        phoneNumber: form.phoneNumber,
        hospitalAffiliation: form.hospitalAffiliation,
        yearsOfExperience: form.yearsOfExperience ? Number(form.yearsOfExperience) : null,
        consultationFee: form.consultationFee ? Number(form.consultationFee) : null,
      });
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
            <label>Specialization</label>
            <input type="text" value={form.specialization} onChange={e => setForm({...form, specialization: e.target.value})} />
          </div>
          <div className="form-group">
            <label>Phone</label>
            <input type="text" value={form.phoneNumber} onChange={e => setForm({...form, phoneNumber: e.target.value})} />
          </div>
          <div className="form-group">
            <label>Hospital Affiliation</label>
            <input type="text" value={form.hospitalAffiliation} onChange={e => setForm({...form, hospitalAffiliation: e.target.value})} />
          </div>
          <div className="form-group">
            <label>Years of Experience</label>
            <input type="number" value={form.yearsOfExperience} onChange={e => setForm({...form, yearsOfExperience: e.target.value})} />
          </div>
          <div className="form-group">
            <label>Consultation Fee ($)</label>
            <input type="number" step="0.01" value={form.consultationFee} onChange={e => setForm({...form, consultationFee: e.target.value})} />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">Save</button>
            <button type="button" className="btn btn-secondary" onClick={() => setEditing(false)}>Cancel</button>
          </div>
        </form>
      ) : (
        <div className="profile-details">
          <p><strong>Name:</strong> {profile.name || profile.user?.username || '—'}</p>
          <p><strong>Email:</strong> {profile.email || profile.user?.email || '—'}</p>
          <p><strong>Specialization:</strong> {profile.specialization || '—'}</p>
          <p><strong>Phone:</strong> {profile.phoneNumber || '—'}</p>
          <p><strong>Hospital:</strong> {profile.hospitalAffiliation || '—'}</p>
          <p><strong>Experience:</strong> {profile.yearsOfExperience ? `${profile.yearsOfExperience} years` : '—'}</p>
          <p><strong>Consultation Fee:</strong> {profile.consultationFee ? `$${profile.consultationFee}` : '—'}</p>
          <p><strong>License:</strong> {profile.licenseNumber || '—'}</p>
        </div>
      )}
    </div>
  );
}
