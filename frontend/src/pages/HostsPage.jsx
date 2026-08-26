import { useEffect, useState } from 'react'
import { createHost, deleteHost, listHosts, updateHost } from '../api/hostsApi'
import HostForm from '../components/HostForm'
import HostsTable from '../components/HostsTable'
import { useAuth } from '../context/AuthContext'

// null = form closed, 'new' = creating, a host object = editing that host
export default function HostsPage() {
  const [hosts, setHosts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [formTarget, setFormTarget] = useState(null)
  const { logout } = useAuth()

  const refresh = () => {
    setLoading(true)
    listHosts()
      .then(setHosts)
      .catch(() => setError('Could not load hosts'))
      .finally(() => setLoading(false))
  }

  useEffect(refresh, [])

  const handleCreate = async (values) => {
    await createHost(values)
    setFormTarget(null)
    refresh()
  }

  const handleUpdate = async (values) => {
    await updateHost(formTarget.id, values)
    setFormTarget(null)
    refresh()
  }

  const handleDelete = async (host) => {
    if (!window.confirm(`Delete host "${host.domeniu}"?`)) {
      return
    }
    await deleteHost(host.id)
    refresh()
  }

  return (
    <div className="page">
      <header className="page-header">
        <h1>Proxy Hosts</h1>
        <button type="button" onClick={logout}>
          Log out
        </button>
      </header>

      {error && <p className="error">{error}</p>}

      {formTarget === null && (
        <button type="button" onClick={() => setFormTarget('new')}>
          Add host
        </button>
      )}

      {formTarget === 'new' && (
        <HostForm onSubmit={handleCreate} onCancel={() => setFormTarget(null)} />
      )}

      {formTarget && formTarget !== 'new' && (
        <HostForm
          initialValue={formTarget}
          onSubmit={handleUpdate}
          onCancel={() => setFormTarget(null)}
        />
      )}

      {loading ? (
        <p>Loading...</p>
      ) : (
        <HostsTable hosts={hosts} onEdit={setFormTarget} onDelete={handleDelete} />
      )}
    </div>
  )
}
