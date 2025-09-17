import axios from 'axios'

// Axios 인스턴스 생성
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// --> Access Token 만료 체크 함수
function isTokenExpired(token) {
  if (!token) return true
  const payload = JSON.parse(atob(token.split('.')[1]))
  const now = Math.floor(Date.now() / 1000)
  return payload.exp < now
}


// ---> Refresh Token 요청
async function refreshToken() {
  const refreshToken = localStorage.getItem('refreshToken')
  if (!refreshToken) throw new Error('Refresh Token 없음')

  const response = await axios.post(`${import.meta.env.VITE_API_URL}/auth/refresh`, {
    refreshToken,
  })

  const { accessToken, refreshToken: newRefreshToken } = response.data

  localStorage.setItem('jwt', accessToken)
  if (newRefreshToken) localStorage.setItem('refreshToken', newRefreshToken)

  return accessToken
}

// 요청 인터셉터
api.interceptors.request.use(
  async (config) => { // <-- async 추가
    let token = localStorage.getItem('jwt')
    console.log('🔍 JWT 토큰:', token ? '존재함' : '없음')

    if (token && isTokenExpired(token)) {
      try {
        token = await refreshToken()
        console.log('🔄 Access Token 갱신 완료')
      } catch (err) {
        console.error('Refresh Token 오류:', err)
        localStorage.clear()
        window.location.href = '/login'
        throw err
      }
    }

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      console.log('🔍 Authorization 헤더:', config.headers.Authorization.substring(0, 20) + '...')
    }

    console.log('🔍 요청 URL:', config.baseURL + config.url)
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