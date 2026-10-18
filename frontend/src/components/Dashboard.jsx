import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { createEvent, getSpendSummary, listEvents } from '../api'
import { useAuth } from '../auth'

const CATEGORIES = ['Travel', 'Food', 'Retail', 'Subscriptions', 'Support']
const EVENT_TYPES = ['PURCHASE', 'REFUND', 'INTERACTION', 'SUBSCRIPTION']

function money(value) {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 2,
  }).format(Number(value || 0))
}

function sampleEvent() {
  const eventType = EVENT_TYPES[Math.floor(Math.random() * EVENT_TYPES.length)]
  const category = CATEGORIES[Math.floor(Math.random() * CATEGORIES.length)]
  const amount =
    eventType === 'INTERACTION' ? 0 : Number((Math.random() * 280 + 12).toFixed(2))
  const daysAgo = Math.floor(Math.random() * 28)
  return {
    customerId: `cust-${1000 + Math.floor(Math.random() * 8)}`,
    eventType,
    category,
    amount,
    currency: 'USD',
    channel: 'web',
    description: `${eventType.toLowerCase()} via ${category}`,
    occurredAt: new Date(Date.now() - daysAgo * 24 * 60 * 60 * 1000).toISOString(),
    metadata: { source: 'dashboard-seed' },
  }
}

export default function Dashboard() {
  const { auth, token, clearSession } = useAuth()
  const [summary, setSummary] = useState(null)
  const [events, setEvents] = useState([])
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [form, setForm] = useState({
    customerId: 'cust-1001',
    eventType: 'PURCHASE',
    category: 'Travel',
    amount: '120.00',
    currency: 'USD',
    channel: 'web',
    description: '',
    occurredAt: new Date().toISOString().slice(0, 16),
  })

  const load = useCallback(async () => {
    setError('')
    try {
      const [summaryData, eventsData] = await Promise.all([
        getSpendSummary(token),
        listEvents(token, { size: 12 }),
      ])
      setSummary(summaryData)
      setEvents(eventsData.content || [])
    } catch (err) {
      setError(err.message || 'Failed to load dashboard')
      if (err.status === 401 || err.status === 403) clearSession()
    }
  }, [token, clearSession])

  useEffect(() => {
    load()
  }, [load])

  const trendData = useMemo(
    () =>
      (summary?.dailyTrend || []).map((point) => ({
        date: point.date,
        netSpend: Number(point.netSpend),
        eventCount: point.eventCount,
      })),
    [summary],
  )

  const categoryData = useMemo(
    () =>
      (summary?.byCategory || []).map((point) => ({
        category: point.category,
        netSpend: Number(point.netSpend),
      })),
    [summary],
  )

  function updateField(field) {
    return (event) => setForm((prev) => ({ ...prev, [field]: event.target.value }))
  }

  async function submitEvent(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      await createEvent(token, {
        ...form,
        amount: Number(form.amount),
        occurredAt: new Date(form.occurredAt).toISOString(),
      })
      await load()
    } catch (err) {
      setError(err.message || 'Could not create event')
    } finally {
      setBusy(false)
    }
  }

  async function seedSamples() {
    setBusy(true)
    setError('')
    try {
      for (let i = 0; i < 8; i += 1) {
        await createEvent(token, sampleEvent())
      }
      await load()
    } catch (err) {
      setError(err.message || 'Could not seed sample events')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="stack">
      <div className="section-title">
        <div>
          <h2>Spend trends</h2>
          <p className="muted">Signed in as {auth?.fullName || auth?.email}</p>
        </div>
        <div className="actions">
          <button type="button" className="ghost-btn" onClick={seedSamples} disabled={busy}>
            Seed sample events
          </button>
          <button type="button" className="ghost-btn" onClick={clearSession}>
            Sign out
          </button>
        </div>
      </div>

      {error ? <p className="error-banner">{error}</p> : null}

      <div className="metrics">
        <div className="metric">
          <span>Net spend</span>
          <strong>{money(summary?.netSpend)}</strong>
        </div>
        <div className="metric">
          <span>Events</span>
          <strong>{summary?.totalEvents ?? 0}</strong>
        </div>
        <div className="metric">
          <span>Customers</span>
          <strong>{summary?.uniqueCustomers ?? 0}</strong>
        </div>
      </div>

      <div className="dashboard-grid">
        <section className="panel">
          <div className="section-title">
            <h2>Daily net spend</h2>
          </div>
          <div className="chart-wrap">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={trendData}>
                <CartesianGrid stroke="rgba(232,242,240,0.08)" vertical={false} />
                <XAxis dataKey="date" stroke="#9bb4b0" tick={{ fontSize: 12 }} />
                <YAxis stroke="#9bb4b0" tick={{ fontSize: 12 }} />
                <Tooltip
                  contentStyle={{
                    background: '#0f2a2e',
                    border: '1px solid rgba(232,242,240,0.12)',
                    borderRadius: 12,
                  }}
                />
                <Line type="monotone" dataKey="netSpend" stroke="#3ddc97" strokeWidth={3} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </section>

        <section className="panel">
          <div className="section-title">
            <h2>By category</h2>
          </div>
          <div className="chart-wrap">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={categoryData}>
                <CartesianGrid stroke="rgba(232,242,240,0.08)" vertical={false} />
                <XAxis dataKey="category" stroke="#9bb4b0" tick={{ fontSize: 12 }} />
                <YAxis stroke="#9bb4b0" tick={{ fontSize: 12 }} />
                <Tooltip
                  contentStyle={{
                    background: '#0f2a2e',
                    border: '1px solid rgba(232,242,240,0.12)',
                    borderRadius: 12,
                  }}
                />
                <Bar dataKey="netSpend" fill="#f4c95f" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </section>
      </div>

      <div className="dashboard-grid">
        <section className="panel">
          <div className="section-title">
            <h2>Ingest event</h2>
          </div>
          <form className="form-grid" onSubmit={submitEvent}>
            <label>
              Customer ID
              <input required value={form.customerId} onChange={updateField('customerId')} />
            </label>
            <label>
              Event type
              <select value={form.eventType} onChange={updateField('eventType')}>
                {EVENT_TYPES.map((type) => (
                  <option key={type} value={type}>
                    {type}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Category
              <select value={form.category} onChange={updateField('category')}>
                {CATEGORIES.map((category) => (
                  <option key={category} value={category}>
                    {category}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Amount
              <input
                required
                type="number"
                min="0"
                step="0.01"
                value={form.amount}
                onChange={updateField('amount')}
              />
            </label>
            <label>
              Occurred at
              <input
                required
                type="datetime-local"
                value={form.occurredAt}
                onChange={updateField('occurredAt')}
              />
            </label>
            <label>
              Description
              <input value={form.description} onChange={updateField('description')} placeholder="Optional note" />
            </label>
            <button className="primary-btn" type="submit" disabled={busy}>
              {busy ? 'Saving...' : 'Create event'}
            </button>
          </form>
        </section>

        <section className="panel">
          <div className="section-title">
            <h2>Recent events</h2>
          </div>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Customer</th>
                  <th>Type</th>
                  <th>Category</th>
                  <th>Amount</th>
                  <th>When</th>
                </tr>
              </thead>
              <tbody>
                {events.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="muted">
                      No events yet. Create one or seed samples.
                    </td>
                  </tr>
                ) : (
                  events.map((event) => (
                    <tr key={event.id}>
                      <td>{event.customerId}</td>
                      <td>{event.eventType}</td>
                      <td>{event.category}</td>
                      <td>{money(event.amount)}</td>
                      <td>{new Date(event.occurredAt).toLocaleString()}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </div>
  )
}
