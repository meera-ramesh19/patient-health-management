import api from './client';

export const patientApi = {
  getDashboard: () => api.get('/patient/dashboard'),
  getProfile: () => api.get('/patient/profile'),
  updateProfile: (data: any) => api.put('/patient/profile', data),
  getVisits: () => api.get('/patient/visits'),
  getPrescriptions: () => api.get('/patient/prescriptions'),
  getActivePrescriptions: () => api.get('/patient/prescriptions/active'),
  getLabOrders: () => api.get('/patient/lab-orders'),
  getLabOrdersByStatus: (status: string) => api.get(`/patient/lab-orders/status/${status}`),
  getAppointments: () => api.get('/patient/appointments'),
  bookAppointment: (data: any) => api.post('/patient/appointments', data),
  cancelAppointment: (id: number) => api.delete(`/patient/appointments/${id}`),
  getMyDoctor: () => api.get('/patient/doctors'),
};
