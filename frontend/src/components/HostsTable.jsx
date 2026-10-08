export default function HostsTable({ hosts, onEdit, onDelete }) {
  if (hosts.length === 0) {
    return (
      <div className="card table-card">
        <p className="empty-state">No hosts configured yet. Add your first one above.</p>
      </div>
    )
  }

  return (
    <div className="card table-card">
      <div className="table-scroll">
        <table className="table">
          <thead>
            <tr>
              <th>Domain</th>
              <th>Target URL</th>
              <th>Active</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {hosts.map((host) => (
              <tr key={host.id}>
                <td className="domain-cell" data-label="Domain">
                  {host.domeniu}
                </td>
                <td className="url-cell" data-label="Target URL">
                  {host.targetUrl}
                </td>
                <td data-label="Active">
                  <span
                    className={`status-badge ${host.activ ? 'status-active' : 'status-inactive'}`}
                  >
                    <span className="status-dot" />
                    {host.activ ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td className="row-actions">
                  <button type="button" className="btn btn-secondary btn-sm" onClick={() => onEdit(host)}>
                    Edit
                  </button>
                  <button type="button" className="btn btn-danger btn-sm" onClick={() => onDelete(host)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
