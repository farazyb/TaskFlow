import { Link } from 'react-router-dom'
import { useAuth } from '../context/auth-context.js'

function LoginPage() {
  const { login } = useAuth()

  const handleDemoLogin = () => {
    login('demo-token', { name: 'Faraz' })
  }

  return (
    <section className="auth-page">
      <div className="auth-card">
        <p className="eyebrow">TaskFlow Access</p>
        <h1>Sign in</h1>
        <p className="subtle">
          Stub page for TF-22. Full form validation and API wiring land in TF-23/TF-24.
        </p>

        <button type="button" className="primary-button" onClick={handleDemoLogin}>
          Demo Login (Set In-Memory Token)
        </button>

        <p className="helper-links">
          No account yet? <Link to="/register">Register</Link>
        </p>
      </div>
    </section>
  )
}

export default LoginPage
