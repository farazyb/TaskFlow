import axios from 'axios'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import apiClient from '../services/api.js'

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function validateRegister(form) {
  const errors = {}

  if (!form.fullName.trim()) {
    errors.fullName = 'Full name is required.'
  }

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

function RegisterPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
  })
  const [fieldErrors, setFieldErrors] = useState({})
  const [submitError, setSubmitError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

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

    const errors = validateRegister(form)
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors)
      return
    }

    setIsSubmitting(true)
    setSubmitError('')
    setFieldErrors({})

    try {
      await apiClient.post('/api/v1/auth/register', {
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        password: form.password,
      })

      await navigate('/login', {
        replace: true,
        state: { message: 'Account created successfully. You can sign in now.' },
      })
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const status = error.response?.status
        const payload = error.response?.data

        if (status === 400 && payload?.fieldErrors) {
          setFieldErrors(payload.fieldErrors)
          setSubmitError(payload.message || 'Please fix the highlighted fields.')
        } else if (status === 409) {
          setFieldErrors({ email: 'This email address is already registered.' })
          setSubmitError(payload?.message || 'Email already exists.')
        } else if (status === 500) {
          setSubmitError('Server error. Please try again in a moment.')
        } else {
          setSubmitError(payload?.message || 'Unable to register right now. Please try again.')
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
        <p className="eyebrow">TaskFlow Onboarding</p>
        <h1>Create account</h1>
        <p className="subtle">
          Create your TaskFlow identity to collaborate across projects.
        </p>

        {submitError ? <p className="form-error-banner">{submitError}</p> : null}

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label htmlFor="fullName">Full name</label>
            <input
              id="fullName"
              name="fullName"
              type="text"
              autoComplete="name"
              className={fieldErrors.fullName ? 'form-input input-error' : 'form-input'}
              value={form.fullName}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="Faraz Yazdani"
            />
            {fieldErrors.fullName ? <p className="field-error">{fieldErrors.fullName}</p> : null}
          </div>

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
              autoComplete="new-password"
              className={fieldErrors.password ? 'form-input input-error' : 'form-input'}
              value={form.password}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="At least 8 characters"
            />
            {fieldErrors.password ? <p className="field-error">{fieldErrors.password}</p> : null}
          </div>

          <button type="submit" className="primary-button" disabled={isSubmitting}>
            {isSubmitting ? 'Creating account...' : 'Create account'}
          </button>
        </form>

        <p className="helper-links auth-footnote">
          Already registered? <Link to="/login">Go to login</Link>
        </p>
      </div>
    </section>
  )
}

export default RegisterPage
