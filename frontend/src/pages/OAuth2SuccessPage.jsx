import { Link } from 'react-router-dom'
import { BACKEND_URL } from '../config.js'

function OAuth2SuccessPage() {
  return (
    <section className="auth-page">
      <div className="auth-card">
        <p className="eyebrow">OAuth2 Callback</p>
        <h1>Google sign-in callback</h1>
        <p className="subtle">
          This route is ready for token exchange wiring in the next auth task.
        </p>
        <p className="subtle">
          Backend base URL from config: <code>{BACKEND_URL}</code>
        </p>
        <p className="helper-links">
          Continue to <Link to="/login">login</Link>.
        </p>
      </div>
    </section>
  )
}

export default OAuth2SuccessPage
