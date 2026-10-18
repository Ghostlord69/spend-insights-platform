const API_BASE = import.meta.env.VITE_API_BASE_URL || ''

function authHeaders(token) {
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  }
}

async function parseResponse(response) {
  const text = await response.text()
  const data = text ? JSON.parse(text) : null
  if (!response.ok) {
    const message = data?.message || data?.error || `Request failed (${response.status})`
    const error = new Error(message)
    error.status = response.status
    error.fieldErrors = data?.fieldErrors || []
    throw error
  }
  return data
}

export async function register(payload) {
  const response = await fetch(`${API_BASE}/api/v1/auth/register`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  })
  return parseResponse(response)
}

export async function login(payload) {
  const response = await fetch(`${API_BASE}/api/v1/auth/login`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  })
  return parseResponse(response)
}

export async function createEvent(token, payload) {
  const response = await fetch(`${API_BASE}/api/v1/events`, {
    method: 'POST',
    headers: authHeaders(token),
    body: JSON.stringify(payload),
  })
  return parseResponse(response)
}

export async function listEvents(token, { customerId, page = 0, size = 20 } = {}) {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (customerId) params.set('customerId', customerId)
  const response = await fetch(`${API_BASE}/api/v1/events?${params}`, {
    headers: authHeaders(token),
  })
  return parseResponse(response)
}

export async function getSpendSummary(token, { from, to } = {}) {
  const params = new URLSearchParams()
  if (from) params.set('from', from)
  if (to) params.set('to', to)
  const query = params.toString()
  const response = await fetch(`${API_BASE}/api/v1/analytics/spend-summary${query ? `?${query}` : ''}`, {
    headers: authHeaders(token),
  })
  return parseResponse(response)
}
