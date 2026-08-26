import { createContext, useContext, useMemo, useState } from 'react'
import { login as loginRequest } from '../api/authApi'
import { clearToken, getToken, setToken } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  // Read any token left over from a previous visit, so a page refresh doesn't
  // log the user out.
  const [token, setTokenState] = useState(() => getToken())

  const login = async (username, password) => {
    const newToken = await loginRequest(username, password)
    setToken(newToken)
    setTokenState(newToken)
  }

  const logout = () => {
    clearToken()
    setTokenState(null)
  }

  const value = useMemo(
    () => ({ isAuthenticated: Boolean(token), login, logout }),
    [token],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
