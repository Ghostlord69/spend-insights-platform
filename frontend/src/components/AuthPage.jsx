import { useState } from 'react'
import { login, register } from '../api'
import { useAuth } from '../auth'

export default function AuthPage() {
  const { setSession } = useAuth()
  const [mode, setMode] = useState('login')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [form, setForm] = useState({
    email: '',
    password: '',
    fullName: '',
  })

  function updateField(field) {
    return (event) => setForm((prev) => ({ ...prev, [field]: event.target.value }))
  }

  async function onSubmit(event) {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      const payload =
        mode === 'login'
          ? { email: form.email, password: form.password }
          : { email: form.email, password: form.password, fullName: form.fullName }
      const session = mode === 'login' ? await login(payload) : await register(payload)
      setSession(session)
    } catch (err) {
      setError(err.message || 'Authentication failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="hero-auth">
      <section className="hero-copy">
        <p className="muted">Customer Spend Insights</p>
        <h2>Collect events. See spend trends with clarity.</h2>
        <p>
          JWT-secured Spring Boot APIs ingest purchase, refund, subscription, and interaction
          events from this React channel into PostgreSQL for analytics-ready reporting.
        </p>
      </section>

      <section className="panel">
        <div className="auth-tabs">
          <button type="button" className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>
            Sign in
          </button>
          <button
            type="button"
            className={mode === 'register' ? 'active' : ''}
            onClick={() => setMode('register')}
          >
            Create account
          </button>
        </div>

        {error ? <p className="error-banner">{error}</p> : null}

        <form className="form-grid" onSubmit={onSubmit}>
          {mode === 'register' ? (
            <label>
              Full name
              <input required value={form.fullName} onChange={updateField('fullName')} placeholder="Alex Rivera" />
            </label>
          ) : null}
          <label>
            Email
            <input
              required
              type="email"
              value={form.email}
              onChange={updateField('email')}
              placeholder="analyst@company.com"
            />
          </label>
          <label>
            Password
            <input
              required
              type="password"
              minLength={8}
              value={form.password}
              onChange={updateField('password')}
              placeholder="At least 8 characters"
            />
          </label>
          <button className="primary-btn" type="submit" disabled={loading}>
            {loading ? 'Working...' : mode === 'login' ? 'Enter dashboard' : 'Create and continue'}
          </button>
        </form>
      </section>
    </div>
  )
}
