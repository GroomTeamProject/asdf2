import { getCookie } from '../utils/cookie'

const API_URL = import.meta.env.VITE_API_URL

async function handleResponse(response) {
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({ message: '알 수 없는 서버 에러' }))
    throw new Error(errorData.message || `HTTP Error: ${response.status}`)
  }
  
  return response.status !== 204 ? response.json().catch(() => null) : null
}

async function request(url, method, data) {
  //const csrfToken = getCookie('XSRF-TOKEN')

  const options = {
    method,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      //...(csrfToken && { 'X-CSRF-TOKEN': csrfToken }), // ✅ 추가
    },
    body: data ? JSON.stringify(data) : undefined,
  }

  const response = await fetch(`${API_URL}${url}`, options)
  return handleResponse(response)
}

export const GET = url => request(url, 'GET')
export const POST = (url, data) => request(url, 'POST', data)
export const PUT = (url, data) => request(url, 'PUT', data)
export const PATCH = (url, data) => request(url, 'PATCH', data)
export const DELETE = url => request(url, 'DELETE')
