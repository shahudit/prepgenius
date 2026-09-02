import api from './api'

export const getAdminCompanies = (params = {}) =>
  api.get('/api/admin/companies', {
    params: { size: 200, ...params }
  })

export const createCompany = (data) =>
  api.post('/api/admin/companies', data)

export const updateCompanyById = (id, data) =>
  api.put(`/api/admin/companies/${id}`, data)

export const deleteCompanyById = (id) =>
  api.delete(`/api/admin/companies/${id}`)

export const getAdminCategories = (params = {}) =>
  api.get('/api/admin/categories', {
    params: { size: 200, ...params }
  })

export const createCategory = (data) =>
  api.post('/api/admin/categories', data)

export const updateCategoryById = (id, data) =>
  api.put(`/api/admin/categories/${id}`, data)

export const deleteCategoryById = (id) =>
  api.delete(`/api/admin/categories/${id}`)

export const getAdminUsers = (params = {}) =>
  api.get('/api/admin/users', {
    params: { size: 200, ...params }
  })

export const deleteUserById = (id) =>
  api.delete(`/api/admin/users/${id}`)

export const getAdminDashboardStats = () =>
  api.get('/api/admin/dashboard', {
    headers: {
      Accept: 'application/json'
    }
  })

export const getAdminReports = () =>
  api.get('/api/admin/reports', {
    headers: {
      Accept: 'application/json'
    }
  })
