import axios from 'axios'
import { BACKEND_URL } from '../config.js'

const apiClient = axios.create({
  baseURL: BACKEND_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

const AUTH_ENDPOINTS = [
  '/api/v1/auth/login',
  '/api/v1/auth/register',
  '/api/v1/auth/refresh',
]

let getAccessToken = () => null
let updateAccessToken = () => {}
let handleAuthFailure = () => {}
let refreshPromise = null

function isAuthEndpoint(url = '') {
  return AUTH_ENDPOINTS.some((endpoint) => url.includes(endpoint))
}

export function configureApiAuth({
  getAccessToken: tokenReader,
  setAccessToken: tokenWriter,
  onAuthFailure,
} = {}) {
  if (typeof tokenReader === 'function') {
    getAccessToken = tokenReader
  }
  if (typeof tokenWriter === 'function') {
    updateAccessToken = tokenWriter
  }
  if (typeof onAuthFailure === 'function') {
    handleAuthFailure = onAuthFailure
  }
}

apiClient.interceptors.request.use((config) => {
  const accessToken = getAccessToken()
  if (accessToken) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    const status = error.response?.status

    if (!originalRequest || status !== 401) {
      return Promise.reject(error)
    }

    if (originalRequest._retry || isAuthEndpoint(originalRequest.url || '')) {
      return Promise.reject(error)
    }

    originalRequest._retry = true

    if (!refreshPromise) {
      refreshPromise = apiClient
        .post('/api/v1/auth/refresh')
        .then((response) => {
          const newToken = response.data?.accessToken
          if (!newToken) {
            throw new Error('Refresh succeeded without an access token.')
          }
          updateAccessToken(newToken)
          return newToken
        })
        .catch((refreshError) => {
          handleAuthFailure()
          throw refreshError
        })
        .finally(() => {
          refreshPromise = null
        })
    }

    try {
      const refreshedToken = await refreshPromise
      originalRequest.headers = originalRequest.headers ?? {}
      originalRequest.headers.Authorization = `Bearer ${refreshedToken}`
      return apiClient(originalRequest)
    } catch (refreshError) {
      return Promise.reject(refreshError)
    }
  },
)

export default apiClient
