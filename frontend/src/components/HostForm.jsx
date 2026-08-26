import { useState } from 'react'

const emptyValues = { domeniu: '', targetUrl: '', activ: true }

export default function HostForm({ initialValue, onSubmit, onCancel }) {
  const [values, setValues] = useState(initialValue ?? emptyValues)
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const isEditing = Boolean(initialValue)

  const handleChange = (field) => (event) => {
    const value = field === 'activ' ? event.target.checked : event.target.value
    setValues((prev) => ({ ...prev, [field]: value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await onSubmit(values)
    } catch (err) {
      const message = err.response?.data?.message ?? 'Save failed'
      setError(message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="card host-form" onSubmit={handleSubmit}>
      <h2>{isEditing ? 'Edit host' : 'Add host'}</h2>
      {error && <p className="error">{error}</p>}
      <label>
        Domain
        <input
          value={values.domeniu}
          onChange={handleChange('domeniu')}
          placeholder="app.example.local"
          required
        />
      </label>
      <label>
        Target URL
        <input
          value={values.targetUrl}
          onChange={handleChange('targetUrl')}
          placeholder="http://localhost:3000"
          required
        />
      </label>
      <label className="checkbox-label">
        <input type="checkbox" checked={values.activ} onChange={handleChange('activ')} />
        Active
      </label>
      <div className="form-actions">
        <button type="submit" disabled={submitting}>
          {submitting ? 'Saving...' : 'Save'}
        </button>
        <button type="button" onClick={onCancel} disabled={submitting}>
          Cancel
        </button>
      </div>
    </form>
  )
}
