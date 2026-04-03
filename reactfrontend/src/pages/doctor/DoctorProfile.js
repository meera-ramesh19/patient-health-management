import { useState, useEffect, useActionState } from 'react';
import { doctorApi } from '../../api/doctor';

export default function DoctorProfile() {
  const [profile, setProfile] = useState(null);
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    doctorApi.getProfile()
      .then(res => setProfile(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  // React 19: useActionState for profile update form
  const [state, saveAction, isSaving] = useActionState(async (_prev, formData) => {
    try {
      const res = await doctorApi.updateProfile({
        specialization: formData.get('specialization'),
        phoneNumber: formData.get('phoneNumber'),
        hospitalAffiliation: formData.get('hospitalAffiliation'),
        yearsOfExperience: formData.get('yearsOfExperience') ? Number(formData.get('yearsOfExperience')) : null,
        consultationFee: formData.get('consultationFee') ? Number(formData.get('consultationFee')) : null,
      });
      setProfile(res.data);
      setEditing(false);
      return { error: null, success: 'Profile updated!' };
    } catch (err) {
      return { error: err.response?.data?.message || 'Update failed.', success: null };
    }
  }, { error: null, success: null });

  if (loading) return <div className="loading">Loading...</div>;
  if (!profile) return <div className="error-message">Failed to load profile.</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>My Profile</h1>
        {!editing && <button className="btn btn-primary" onClick={() => setEditing(true)}>Edit</button>}
      </div>

      {state.error && <div className="error-message">{state.error}</div>}
      {state.success && <div className="success-message">{state.success}</div>}

      {editing ? (
        <form className="profile-form" action={saveAction}>
          <div className="form-group">
            <label>Specialization</label>
            <input type="text" name="specialization" defaultValue={profile.specialization || ''} />
          </div>
          <div className="form-group">
            <label>Phone</label>
            <input type="text" name="phoneNumber" defaultValue={profile.phoneNumber || ''} />
          </div>
          <div className="form-group">
            <label>Hospital Affiliation</label>
            <input type="text" name="hospitalAffiliation" defaultValue={profile.hospitalAffiliation || ''} />
          </div>
          <div className="form-group">
            <label>Years of Experience</label>
            <input type="number" name="yearsOfExperience" defaultValue={profile.yearsOfExperience?.toString() || ''} />
          </div>
          <div className="form-group">
            <label>Consultation Fee ($)</label>
            <input type="number" step="0.01" name="consultationFee" defaultValue={profile.consultationFee?.toString() || ''} />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary" disabled={isSaving}>
              {isSaving ? 'Saving...' : 'Save'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => setEditing(false)}>Cancel</button>
          </div>
        </form>
      ) : (
        <div className="profile-details">
          <p><strong>Name:</strong> {profile.name || profile.user?.username || '\u2014'}</p>
          <p><strong>Email:</strong> {profile.email || profile.user?.email || '\u2014'}</p>
          <p><strong>Specialization:</strong> {profile.specialization || '\u2014'}</p>
          <p><strong>Phone:</strong> {profile.phoneNumber || '\u2014'}</p>
          <p><strong>Hospital:</strong> {profile.hospitalAffiliation || '\u2014'}</p>
          <p><strong>Experience:</strong> {profile.yearsOfExperience ? `${profile.yearsOfExperience} years` : '\u2014'}</p>
          <p><strong>Consultation Fee:</strong> {profile.consultationFee ? `$${profile.consultationFee}` : '\u2014'}</p>
          <p><strong>License:</strong> {profile.licenseNumber || '\u2014'}</p>
        </div>
      )}
    </div>
  );
}
