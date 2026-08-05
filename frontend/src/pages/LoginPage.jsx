import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login } from '../api/client.js';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await login(username, password);
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('username', res.data.username);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.error || 'Login failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="cs-auth-shell">
      <div className="cs-auth-card">
        <div className="cs-logo">Code<span>Sync</span><span className="cs-caret" /></div>
        <div className="cs-subtitle">Log in to your rooms</div>

        <form onSubmit={handleSubmit}>
          <label className="cs-label">Username</label>
          <input className="cs-input" placeholder="e.g. harshit"
                 value={username} onChange={(e) => setUsername(e.target.value)} />

          <label className="cs-label">Password</label>
          <input className="cs-input" type="password" placeholder="********"
                 value={password} onChange={(e) => setPassword(e.target.value)} />

          {error && <div className="cs-error">{error}</div>}

          <button className="cs-btn" type="submit" disabled={loading}>
            {loading ? 'Logging in...' : 'Log in'}
          </button>
        </form>

        <div className="cs-footer-link">
          No account? <Link to="/register">Register</Link>
        </div>
      </div>
    </div>
  );
}
