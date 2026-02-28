import axios from 'axios'
import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/auth-context.js'
import apiClient from '../services/api.js'

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function validateLogin(form) {
  const errors = {}

  if (!form.email.trim()) {
    errors.email = 'Email is required.'
  } else if (!EMAIL_REGEX.test(form.email.trim())) {
    errors.email = 'Enter a valid email address.'
  }

  if (!form.password) {
    errors.password = 'Password is required.'
  } else if (form.password.length < 8) {
    errors.password = 'Password must be at least 8 characters.'
  }

  return errors
}

function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [form, setForm] = useState({
    email: '',
    password: '',
  })
  const [fieldErrors, setFieldErrors] = useState({})
  const [submitError, setSubmitError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const successMessage = location.state?.message

  const handleChange = (event) => {
    const { name, value } = event.target

    setForm((prev) => ({
      ...prev,
      [name]: value,
    }))

    setFieldErrors((prev) => ({
      ...prev,
      [name]: '',
    }))
    setSubmitError('')
  }

  const handleSubmit = async (event) => {
    event.preventDefault()

    const errors = validateLogin(form)
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors)
      return
    }

    setIsSubmitting(true)
    setSubmitError('')
    setFieldErrors({})

    try {
      const response = await apiClient.post('/api/v1/auth/login', {
        email: form.email.trim(),
        password: form.password,
      })

      const accessToken = response.data?.accessToken
      if (!accessToken) {
        setSubmitError('Login succeeded but no access token was returned.')
        return
      }

      login(accessToken, { name: form.email.trim(), email: form.email.trim() })
      await navigate('/dashboard', { replace: true })
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const status = error.response?.status
        const payload = error.response?.data

        if (status === 400 && payload?.fieldErrors) {
          setFieldErrors(payload.fieldErrors)
          setSubmitError(payload.message || 'Please fix the highlighted fields.')
        } else if (status === 401) {
          setSubmitError('Invalid email or password.')
        } else if (status === 500) {
          setSubmitError('Server error. Please try again in a moment.')
        } else {
          setSubmitError(payload?.message || 'Unable to login right now. Please try again.')
        }
      } else {
        setSubmitError('Unexpected error. Please try again.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="auth-page">
      <div className="auth-card">
        <p className="eyebrow">TaskFlow Access</p>
        <h1>Sign in</h1>
        <p className="subtle">
          Continue with your workspace. Access token is held in memory and never persisted in
          local storage.
        </p>

        {successMessage ? <p className="form-success-banner">{successMessage}</p> : null}
        {submitError ? <p className="form-error-banner">{submitError}</p> : null}

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              className={fieldErrors.email ? 'form-input input-error' : 'form-input'}
              value={form.email}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="you@example.com"
            />
            {fieldErrors.email ? <p className="field-error">{fieldErrors.email}</p> : null}
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              className={fieldErrors.password ? 'form-input input-error' : 'form-input'}
              value={form.password}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="Enter your password"
            />
            {fieldErrors.password ? <p className="field-error">{fieldErrors.password}</p> : null}
          </div>

          <button type="submit" className="primary-button" disabled={isSubmitting}>
            {isSubmitting ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <p className="helper-links auth-footnote">
          No account yet? <Link to="/register">Register</Link>
        </p>
      </div>
    </section>
  )
}

export default LoginPage
