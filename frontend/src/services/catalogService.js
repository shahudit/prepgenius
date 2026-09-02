import api from './api';

export const getCompanies = () => api.get('/api/companies');
export const getCategories = () => api.get('/api/categories');
