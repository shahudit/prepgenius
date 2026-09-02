import api from './api'

export const startInterview = (data) =>
  api.post('/api/interviews/start', data)

export const startPractice = () =>
  api.post('/api/interviews/practice/start')

export const submitAnswer = (id, data) =>
  api.post(`/api/interviews/${id}/answers`, data)

export const completeInterview = (id) =>
  api.post(`/api/interviews/${id}/complete`)

export const getInterviewHistory = (params = {}) =>
  api.get('/api/interviews/history', { params })

export const getInterviewDetails = (id) =>
  api.get(`/api/interviews/${id}`)

export const getProgress = () =>
  api.get('/api/interviews/progress')

export const generateQuestions = (data) =>
  api.post('/api/ai/questions/generate', data)

export const evaluateAnswer = (data) =>
  api.post('/api/ai/answers/evaluate', data)

export const generateStudyMaterial = (data) =>
  api.post('/api/ai/study-material', data)