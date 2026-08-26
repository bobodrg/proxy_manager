import client from './client'

export function login(username, password) {
  return client.post('/auth/login', { username, password }).then((res) => res.data.token)
}
