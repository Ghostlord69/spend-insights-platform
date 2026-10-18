import AuthPage from './components/AuthPage'
import Dashboard from './components/Dashboard'
import { useAuth } from './auth'

export default function App() {
  const { token } = useAuth()

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">
          <div className="brand-mark" aria-hidden="true">
            <span />
          </div>
          <div>
            <h1>Spend Insights</h1>
            <p>Event collection & spend analytics</p>
          </div>
        </div>
      </header>
      {token ? <Dashboard /> : <AuthPage />}
    </div>
  )
}
