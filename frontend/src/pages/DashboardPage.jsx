import { BACKEND_URL, WS_URL } from '../config.js'

function DashboardPage() {
  return (
    <section className="dashboard-grid">
      <article className="surface-card">
        <h2>Protected Route Active</h2>
        <p>
          You can see this screen only when an in-memory access token exists in <code>AuthContext</code>.
        </p>
      </article>

      <article className="surface-card">
        <h2>Runtime Config</h2>
        <p>
          <strong>BACKEND_URL:</strong> <code>{BACKEND_URL}</code>
        </p>
        <p>
          <strong>WS_URL:</strong> <code>{WS_URL}</code>
        </p>
      </article>

      <article className="surface-card">
        <h2>Theme Baseline</h2>
        <p>Modern Minimalist tokens are loaded from global CSS custom properties in `index.css`.</p>
        <div className="status-row">
          <span className="status-chip todo">TODO</span>
          <span className="status-chip progress">In Progress</span>
          <span className="status-chip done">Done</span>
        </div>
      </article>
    </section>
  )
}

export default DashboardPage
