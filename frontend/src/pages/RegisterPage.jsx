import { Link } from 'react-router-dom'

function RegisterPage() {
  return (
    <section className="auth-page">
      <div className="auth-card">
        <p className="eyebrow">TaskFlow Onboarding</p>
        <h1>Create account</h1>
        <p className="subtle">
          Registration UI is scaffolded. Field-level validation and submit handling are implemented in
          TF-23.
        </p>

        <button type="button" className="primary-button" disabled>
          Register (Coming Next Task)
        </button>

        <p className="helper-links">
          Already registered? <Link to="/login">Go to login</Link>
        </p>
      </div>
    </section>
  )
}

export default RegisterPage
