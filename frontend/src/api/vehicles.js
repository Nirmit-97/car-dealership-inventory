import api from './axios';

export const getVehicles  = ()          => api.get('/api/vehicles');
export const getVehicle   = (id)        => api.get(`/api/vehicles/${id}`);
export const createVehicle= (data)      => api.post('/api/vehicles', data);
export const updateVehicle= (id, data)  => api.put(`/api/vehicles/${id}`, data);
export const deleteVehicle= (id)        => api.delete(`/api/vehicles/${id}`);
export const searchVehicles = (params)  => api.get('/api/vehicles/search', { params });
export const purchaseVehicle= (id)      => api.post(`/api/vehicles/${id}/purchase`);
export const restockVehicle = (id, amt) => api.post(`/api/vehicles/${id}/restock`, { amount: amt });
