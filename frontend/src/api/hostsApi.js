import client from './client'

export function listHosts() {
  return client.get('/admin/hosts').then((res) => res.data)
}

export function createHost(host) {
  return client.post('/admin/hosts', host).then((res) => res.data)
}

export function updateHost(id, host) {
  return client.put(`/admin/hosts/${id}`, host).then((res) => res.data)
}

export function deleteHost(id) {
  return client.delete(`/admin/hosts/${id}`)
}
