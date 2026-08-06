import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createRoom, getRoom } from '../api/client.js';

export default function HomePage() {
  const [roomName, setRoomName] = useState('');
  const [language, setLanguage] = useState('java');
  const [joinCode, setJoinCode] = useState('');
  const [error, setError] = useState('');
  const [creating, setCreating] = useState(false);
  const [joining, setJoining] = useState(false);
  const navigate = useNavigate();
  const username = localStorage.getItem('username');

  async function handleCreate(e) {
    e.preventDefault();
    setError('');
    setCreating(true);
    try {
      const res = await createRoom(roomName, language);
      navigate(`/room/${res.data.roomCode}`);
    } catch (err) {
      setError(err.response?.data?.error || 'Could not create room');
    } finally {
      setCreating(false);
    }
  }

  async function handleJoin(e) {
    e.preventDefault();
    setError('');
    setJoining(true);
    try {
      await getRoom(joinCode.toUpperCase());
      navigate(`/room/${joinCode.toUpperCase()}`);
    } catch (err) {
      setError('Room not found');
    } finally {
      setJoining(false);
    }
  }

  function logout() {
    localStorage.clear();
    navigate('/login');
  }

  return (
    <div className="cs-home-shell">
      <div className="cs-home-header">
        <div className="cs-greeting">
          Welcome, <span className="cs-accent-text">{username}</span>
        </div>
        <button className="cs-btn cs-btn-ghost" style={{ width: 'auto', padding: '8px 16px', margin: 0 }} onClick={logout}>
          Log out
        </button>
      </div>

      <div className="cs-home-grid">
        <div className="cs-panel">
          <div className="cs-panel-title">Create a room</div>
          <form onSubmit={handleCreate}>
            <label className="cs-label" style={{ marginTop: 0 }}>Room name</label>
            <input className="cs-input" placeholder="e.g. DSA practice"
                   value={roomName} onChange={(e) => setRoomName(e.target.value)} />

            <label className="cs-label">Language</label>
            <select className="cs-select" value={language} onChange={(e) => setLanguage(e.target.value)}>
              <option value="java">Java</option>
              <option value="python">Python</option>
              <option value="javascript">JavaScript</option>
            </select>

            <button className="cs-btn" type="submit" disabled={creating}>
              {creating ? 'Creating...' : 'Create Room'}
            </button>
          </form>
        </div>

        <div className="cs-panel">
          <div className="cs-panel-title">Join a room</div>
          <form onSubmit={handleJoin}>
            <label className="cs-label" style={{ marginTop: 0 }}>Room code</label>
            <input className="cs-input" placeholder="e.g. A1B2C3" style={{ fontFamily: 'var(--font-mono)', letterSpacing: '0.05em' }}
                   value={joinCode} onChange={(e) => setJoinCode(e.target.value)} />

            <button className="cs-btn" type="submit" disabled={joining} style={{ marginTop: 'auto' }}>
              {joining ? 'Joining...' : 'Join Room'}
            </button>
          </form>
        </div>
      </div>

      {error && <div className="cs-error" style={{ marginTop: 20 }}>{error}</div>}
    </div>
  );
}
