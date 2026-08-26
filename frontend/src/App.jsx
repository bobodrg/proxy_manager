import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import LoginPage from './pages/LoginPage'
import HostsPage from './pages/HostsPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/hosts"
        element={
          <ProtectedRoute>
            <HostsPage />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/hosts" replace />} />
    </Routes>
  )
}
