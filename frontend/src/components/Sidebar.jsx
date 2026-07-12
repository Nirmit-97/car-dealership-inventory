import { useAuth } from '../context/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';

export default function Sidebar({ onClose }) {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
    if (onClose) onClose();
  };

  const handleNav = (path) => {
    navigate(path);
    if (onClose) onClose();
  };

  const navItems = [
    { label: 'Dashboard', icon: '📊', path: '/dashboard' },
    { label: 'Inventory',  icon: '🚗', path: '/inventory' },
    ...(isAdmin ? [{ label: 'Manage Stock', icon: '📦', path: '/manage' }] : []),
  ];

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <div className="logo-icon">🚗</div>
        AutoVault
      </div>

      {navItems.map((item) => (
        <button
          key={item.path}
          id={`nav-${item.label.toLowerCase().replace(' ', '-')}`}
          className={`nav-item ${location.pathname === item.path ? 'active' : ''}`}
          onClick={() => handleNav(item.path)}
        >
          <span>{item.icon}</span>
          {item.label}
        </button>
      ))}

      <div className="sidebar-footer">
        <div style={{ padding: '12px', marginBottom: '8px' }}>
          <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '4px' }}>Signed in as</div>
          <div style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '6px', wordBreak: 'break-all' }}>
            {user?.email}
          </div>
          <span className={`role-badge ${isAdmin ? 'admin' : 'user'}`}>
            {isAdmin ? '⚡ Admin' : '👤 User'}
          </span>
        </div>
        <button id="logout-btn" className="nav-item" onClick={handleLogout} style={{ color: 'var(--danger)' }}>
          <span>🚪</span> Sign Out
        </button>
      </div>
    </aside>
  );
}
