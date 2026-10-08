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
    <form className="card form-card" onSubmit={handleSubmit}>
      <h2>{isEditing ? 'Edit host' : 'Add host'}</h2>
      {error && <p className="error">{error}</p>}
      <div className="field">
        <label htmlFor="domeniu">Domain</label>
        <input
          id="domeniu"
          value={values.domeniu}
          onChange={handleChange('domeniu')}
          placeholder="app.example.local"
          required
        />
      </div>
      <div className="field">
        <label htmlFor="targetUrl">Target URL</label>
        <input
          id="targetUrl"
          value={values.targetUrl}
          onChange={handleChange('targetUrl')}
          placeholder="http://localhost:3000"
          required
        />
      </div>
      <div className="field checkbox-field">
        <input
          id="activ"
          type="checkbox"
          checked={values.activ}
          onChange={handleChange('activ')}
        />
        <label htmlFor="activ">Active</label>
      </div>
      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Saving...' : 'Save'}
        </button>
        <button type="button" className="btn btn-secondary" onClick={onCancel} disabled={submitting}>
          Cancel
        </button>
      </div>
    </form>
  )
}
