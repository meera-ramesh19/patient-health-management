import api from './client';

export const patientApi = {
  getDashboard: () => api.get('/patient/dashboard'),
  getProfile: () => api.get('/patient/profile'),
  updateProfile: (data) => api.put('/patient/profile', data),
  getVisits: () => api.get('/patient/visits'),
  getPrescriptions: () => api.get('/patient/prescriptions'),
  getActivePrescriptions: () => api.get('/patient/prescriptions/active'),
  getLabOrders: () => api.get('/patient/lab-orders'),
  getLabOrdersByStatus: (status) => api.get(`/patient/lab-orders/status/${status}`),
  getAppointments: () => api.get('/patient/appointments'),
  bookAppointment: (data) => api.post('/patient/appointments', data),
  cancelAppointment: (id) => api.delete(`/patient/appointments/${id}`),
  getMyDoctor: () => api.get('/patient/doctors'),
};
