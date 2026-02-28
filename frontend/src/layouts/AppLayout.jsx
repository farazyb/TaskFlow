import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../context/auth-context.js'

function AppLayout() {
  const { user, logout } = useAuth()

  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">TaskFlow</p>
          <h1>Dashboard</h1>
        </div>
        <div className="topbar-actions">
          <p className="topbar-user">{user?.name || 'Authenticated User'}</p>
          <button type="button" className="ghost-button" onClick={logout}>
            Logout
          </button>
        </div>
      </header>

      <nav className="tabs" aria-label="Primary">
        <NavLink to="/" end className={({ isActive }) => (isActive ? 'tab active' : 'tab')}>
          Home
        </NavLink>
        <NavLink
          to="/dashboard"
          className={({ isActive }) => (isActive ? 'tab active' : 'tab')}
        >
          Dashboard
        </NavLink>
      </nav>

      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}

export default AppLayout
