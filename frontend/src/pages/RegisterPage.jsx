import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register } from '../api/client.js';

export default function RegisterPage() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await register(username, email, password);
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('username', res.data.username);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.error || 'Registration failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="cs-auth-shell">
      <div className="cs-auth-card">
        <div className="cs-logo">Code<span>Sync</span><span className="cs-caret" /></div>
        <div className="cs-subtitle">Create an account</div>

        <form onSubmit={handleSubmit}>
          <label className="cs-label">Username</label>
          <input className="cs-input" placeholder="e.g. harshit"
                 value={username} onChange={(e) => setUsername(e.target.value)} />

          <label className="cs-label">Email</label>
          <input className="cs-input" placeholder="you@example.com"
                 value={email} onChange={(e) => setEmail(e.target.value)} />

          <label className="cs-label">Password</label>
          <input className="cs-input" type="password" placeholder="At least 6 characters"
                 value={password} onChange={(e) => setPassword(e.target.value)} />

          {error && <div className="cs-error">{error}</div>}

          <button className="cs-btn" type="submit" disabled={loading}>
            {loading ? 'Creating account...' : 'Register'}
          </button>
        </form>

        <div className="cs-footer-link">
          Have an account? <Link to="/login">Log in</Link>
        </div>
      </div>
    </div>
  );
}
