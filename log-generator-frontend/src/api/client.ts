import axios from 'axios'
import type { ApiResponse } from '@/types/api'

const apiClient = axios.create({
  baseURL: '/logger/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.msg || error.message || 'An error occurred'
    return Promise.reject(new Error(message))
  }
)

export async function apiGet<T>(url: string): Promise<T> {
  const response = await apiClient.get<ApiResponse<T>>(url)
  return response.data.data
}

export async function apiPost<T, D = unknown>(url: string, data?: D): Promise<T> {
  const response = await apiClient.post<ApiResponse<T>>(url, data)
  return response.data.data
}

export async function apiPut<T, D = unknown>(url: string, data: D): Promise<T> {
  const response = await apiClient.put<ApiResponse<T>>(url, data)
  return response.data.data
}

export async function apiPatch<T, D = unknown>(url: string, data?: D): Promise<T> {
  const response = await apiClient.patch<ApiResponse<T>>(url, data)
  return response.data.data
}

export async function apiDelete<T, D = unknown>(url: string, data?: D): Promise<T> {
  const response = await apiClient.delete<ApiResponse<T>>(url, { data })
  return response.data.data
}

export default apiClient
