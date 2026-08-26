import axios from 'axios'

const TOKEN_KEY = 'proxy-manager-token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

const client = axios.create()

// Attach the token to every request automatically, so individual API calls
// never have to think about auth headers.
client.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// If the backend ever answers 401 (missing/expired/invalid token), the stored
// token is stale - drop it and send the user back to the login page. A full
// reload is a deliberate simplification: it resets all React state cleanly
// without needing to wire this interceptor into the router/context.
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      clearToken()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default client
