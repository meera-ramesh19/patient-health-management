import api from './client';

export const doctorApi = {
  getDashboard: () => api.get('/doctor/dashboard'),
  getProfile: () => api.get('/doctor/profile'),
  updateProfile: (data) => api.put('/doctor/profile', data),
  getPatients: () => api.get('/doctor/patients'),
  getVisits: () => api.get('/doctor/visits'),
  recordVisit: (data) => api.post('/doctor/visits', data),
  updateVisit: (id, data) => api.put(`/doctor/visits/${id}`, data),
  getPrescriptions: () => api.get('/doctor/prescriptions'),
  writePrescription: (data) => api.post('/doctor/prescriptions', data),
  getLabOrders: () => api.get('/doctor/lab-orders'),
  orderLab: (data) => api.post('/doctor/lab-orders', data),
  updateLabStatus: (id, status) =>
    api.put(`/doctor/lab-orders/${id}/status`, status, { headers: { 'Content-Type': 'text/plain' } }),
  addLabResults: (id, results) =>
    api.put(`/doctor/lab-orders/${id}/results`, results, { headers: { 'Content-Type': 'text/plain' } }),
  getAppointments: () => api.get('/doctor/appointments'),
};
