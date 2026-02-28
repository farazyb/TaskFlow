import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import AuthContext from './auth-context.js'
import { configureApiAuth } from '../services/api.js'

export function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState(null)
  const [user, setUser] = useState(null)
  const accessTokenRef = useRef(accessToken)

  useEffect(() => {
    accessTokenRef.current = accessToken
  }, [accessToken])

  const login = useCallback((token, userData = null) => {
    setAccessToken(token)
    setUser(userData)
  }, [])

  const logout = useCallback(() => {
    setAccessToken(null)
    setUser(null)
  }, [])

  useEffect(() => {
    configureApiAuth({
      getAccessToken: () => accessTokenRef.current,
      setAccessToken,
      onAuthFailure: logout,
    })
  }, [logout])

  const value = useMemo(
    () => ({
      accessToken,
      user,
      login,
      logout,
    }),
    [accessToken, user, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
