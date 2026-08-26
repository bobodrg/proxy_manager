export default function HostsTable({ hosts, onEdit, onDelete }) {
  if (hosts.length === 0) {
    return <p>No hosts configured yet.</p>
  }

  return (
    <table className="hosts-table">
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
            <td>{host.domeniu}</td>
            <td>{host.targetUrl}</td>
            <td>{host.activ ? 'Yes' : 'No'}</td>
            <td className="row-actions">
              <button type="button" onClick={() => onEdit(host)}>
                Edit
              </button>
              <button type="button" onClick={() => onDelete(host)}>
                Delete
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
