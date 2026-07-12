import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { login as loginApi, register as registerApi } from '../api/auth';

export default function LoginPage() {
  const [tab, setTab] = useState('login'); // 'login' | 'register'
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const apiFn = tab === 'login' ? loginApi : registerApi;
      const { data } = await apiFn(form);
      login(data);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong. Try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        {/* Logo */}
        <div className="login-logo">
          <div className="login-logo-icon">🚗</div>
          <div className="login-logo-text">Auto<span>Vault</span></div>
        </div>

        <h1 className="login-title">
          {tab === 'login' ? 'Welcome back' : 'Create account'}
        </h1>
        <p className="login-sub">
          {tab === 'login'
            ? 'Sign in to manage your dealership inventory'
            : 'Register to access the inventory system'}
        </p>

        {/* Tab switcher */}
        <div className="login-tab-bar">
          <button
            className={`login-tab ${tab === 'login' ? 'active' : ''}`}
            onClick={() => { setTab('login'); setError(''); }}
          >Sign In</button>
          <button
            className={`login-tab ${tab === 'register' ? 'active' : ''}`}
            onClick={() => { setTab('register'); setError(''); }}
          >Register</button>
        </div>

        {error && <div className="alert alert-error">⚠ {error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Email address</label>
            <input
              id="email"
              className="form-input"
              name="email"
              type="email"
              placeholder="admin@dealer.com"
              value={form.email}
              onChange={handleChange}
              required
              autoComplete="email"
            />
          </div>
          <div className="form-group">
            <label className="form-label">Password</label>
            <input
              id="password"
              className="form-input"
              name="password"
              type="password"
              placeholder="••••••••"
              value={form.password}
              onChange={handleChange}
              required
              autoComplete="current-password"
            />
          </div>
          <button
            id="login-submit"
            className="btn btn-primary"
            type="submit"
            disabled={loading}
            style={{ width: '100%', justifyContent: 'center', marginTop: 4 }}
          >
            {loading ? '⏳ Please wait…' : tab === 'login' ? '→ Sign In' : '→ Create Account'}
          </button>
        </form>

        <p className="login-footer">
          {tab === 'login'
            ? 'Demo: admin@dealer.com / password (Admin)'
            : 'New accounts are granted USER role by default'}
        </p>
      </div>
    </div>
  );
}
