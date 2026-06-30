import { Routes, Route, Navigate, Link, useNavigate } from 'react-router-dom';
import { useAuth, isAdmin } from './auth';
import LoginPage from './pages/LoginPage';
import EventListPage from './pages/EventListPage';
import EventDetailPage from './pages/EventDetailPage';
import EventFormPage from './pages/EventFormPage';
import DashboardPage from './pages/DashboardPage';

function Nav() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  if (!user) return null;

  return (
    <nav className="nav">
      <Link to="/" className="brand">세투연 행사 플랫폼</Link>
      <Link to="/">행사</Link>
      {isAdmin(user) && <Link to="/admin/events/new">행사 생성</Link>}
      {isAdmin(user) && <Link to="/dashboard">대시보드</Link>}
      <span className="spacer" />
      <span className="user-chip">{user.name} · {user.role}</span>
      <button className="secondary small" onClick={() => { logout(); navigate('/login'); }}>
        로그아웃
      </button>
    </nav>
  );
}

function RequireAuth({ children }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return children;
}

export default function App() {
  return (
    <>
      <Nav />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<RequireAuth><EventListPage /></RequireAuth>} />
        <Route path="/events/:eventId" element={<RequireAuth><EventDetailPage /></RequireAuth>} />
        <Route path="/admin/events/new" element={<RequireAuth><EventFormPage /></RequireAuth>} />
        <Route path="/admin/events/:eventId/edit" element={<RequireAuth><EventFormPage /></RequireAuth>} />
        <Route path="/dashboard" element={<RequireAuth><DashboardPage /></RequireAuth>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}
