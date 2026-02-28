import { useMemo, useState } from 'react'
import AuthContext from './auth-context.js'

export function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState(null)
  const [user, setUser] = useState(null)

  const value = useMemo(
    () => ({
      accessToken,
      user,
      login: (token, userData = null) => {
        setAccessToken(token)
        setUser(userData)
      },
      logout: () => {
        setAccessToken(null)
        setUser(null)
      },
    }),
    [accessToken, user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
