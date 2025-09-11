import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
})

// 요청 인터셉터
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwt')
    console.log('🔍 JWT 토큰:', token ? '존재함' : '없음')  // 👈 디버깅 로그
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      console.log('🔍 Authorization 헤더:', config.headers.Authorization.substring(0, 20) + '...')  // 👈 디버깅 로그
    }
    
    console.log('🔍 요청 URL:', config.baseURL + config.url)  // 👈 디버깅 로그
    return config
  },
  (error) => {
    console.error('❌ 요청 인터셉터 오류:', error)
    return Promise.reject(error)
  }
)

// 응답 인터셉터
api.interceptors.response.use(
  (response) => {
    console.log('✅ API 응답 성공:', response.status)  // 👈 디버깅 로그
    return response
  },
  (error) => {
    console.error('❌ API 응답 오류:', error.response?.status, error.message)  // 👈 디버깅 로그
    
    if (error.response?.status === 401) {
      localStorage.removeItem('jwt')
      localStorage.removeItem('userEmail')
      localStorage.removeItem('userName')
      localStorage.removeItem('userType')
      
      alert('로그인이 만료되었습니다. 다시 로그인해주세요.')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api