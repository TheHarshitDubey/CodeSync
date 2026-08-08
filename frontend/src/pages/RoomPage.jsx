import React, { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import { getDocument, getRoom, executeCode } from '../api/client.js';
import { createSocketClient, sendEdit, sendJoin, sendLeave } from '../api/socket.js';

const DEBOUNCE_MS = 300;

export default function RoomPage() {
  const { roomCode } = useParams();
  const navigate = useNavigate();
  const username = localStorage.getItem('username');

  const [code, setCode] = useState('');
  const [language, setLanguage] = useState('java');
  const [participants, setParticipants] = useState([]);
  const [output, setOutput] = useState('Output will appear here after you run your code.');
  const [running, setRunning] = useState(false);
  const [connected, setConnected] = useState(false);

  const clientRef = useRef(null);
  const versionRef = useRef(0);
  const debounceRef = useRef(null);
  const applyingRemoteChange = useRef(false);

  useEffect(() => {
    let stompClient;

    async function init() {
      const roomRes = await getRoom(roomCode);
      setLanguage(roomRes.data.language);

      const docRes = await getDocument(roomCode);
      setCode(docRes.data.content);
      versionRef.current = docRes.data.version;

      stompClient = createSocketClient({
        roomCode,
        onConnect: () => {
          setConnected(true);
          sendJoin(stompClient, roomCode, username);
        },
        onEdit: (broadcast) => {
          if (broadcast.version <= versionRef.current) return;
          versionRef.current = broadcast.version;
          applyingRemoteChange.current = true;
          setCode(broadcast.content);
        },
        onPresence: (update) => {
          setParticipants(update.participants);
        }
      });

      clientRef.current = stompClient;
      stompClient.activate();
    }

    init();

    return () => {
      if (stompClient) {
        sendLeave(stompClient, roomCode, username);
        stompClient.deactivate();
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [roomCode]);

  function handleEditorChange(value) {
    if (applyingRemoteChange.current) {
      applyingRemoteChange.current = false;
      return;
    }

    setCode(value);

    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      if (clientRef.current && clientRef.current.connected) {
        sendEdit(clientRef.current, roomCode, username, value, versionRef.current);
      }
    }, DEBOUNCE_MS);
  }

  async function handleRun() {
    setRunning(true);
    setOutput('Running...');
    try {
      const res = await executeCode(code, language, '');
      const parts = [];
      if (res.data.stdout) parts.push(res.data.stdout);
      if (res.data.stderr) parts.push('--- stderr ---\n' + res.data.stderr);
      parts.push(`\n[${res.data.status}${res.data.timeSeconds ? `, ${res.data.timeSeconds}s` : ''}]`);
      setOutput(parts.join('\n'));
    } catch (err) {
      setOutput('Execution failed: ' + (err.response?.data?.error || err.message));
    } finally {
      setRunning(false);
    }
  }

  function handleLeaveRoom() {
    if (clientRef.current && clientRef.current.connected) {
      sendLeave(clientRef.current, roomCode, username);
      clientRef.current.deactivate();
    }
    navigate('/');
  }

  function handleLogout() {
    if (clientRef.current && clientRef.current.connected) {
      sendLeave(clientRef.current, roomCode, username);
      clientRef.current.deactivate();
    }
    localStorage.clear();
    navigate('/login');
  }

  return (
    <div className="cs-room-shell">
      <div className="cs-room-topbar">
        <div>
          <span className="cs-room-code">{roomCode}</span>
          <span style={{ marginLeft: 12 }}>
            <span className={`cs-status-dot ${connected ? 'cs-status-connected' : 'cs-status-connecting'}`} />
            {connected ? 'Connected' : 'Connecting...'}
          </span>
        </div>

        <div className="cs-participants">
          Online: {participants.length > 0
            ? participants.map((p, i) => (
                <span key={p}>
                  <span className="cs-participant-chip">{p}</span>{i < participants.length - 1 ? ', ' : ''}
                </span>
              ))
            : '—'}
        </div>

        <button className="cs-run-btn" onClick={handleRun} disabled={running}>
          {running ? 'Running...' : '▶ Run Code'}
        </button>

        <div style={{ display: 'flex', gap: 8 }}>
          <button className="cs-btn cs-btn-ghost" style={{ width: 'auto', padding: '7px 14px', margin: 0, fontSize: 13 }}
                  onClick={handleLeaveRoom}>
            Leave Room
          </button>
          <button className="cs-btn cs-btn-ghost" style={{ width: 'auto', padding: '7px 14px', margin: 0, fontSize: 13 }}
                  onClick={handleLogout}>
            Log out
          </button>
        </div>
      </div>

      <div style={{ flex: 1, display: 'flex' }}>
        <div style={{ flex: 3 }}>
          <Editor
            height="100%"
            language={language}
            value={code}
            onChange={handleEditorChange}
            theme="vs-dark"
            options={{ fontSize: 14, minimap: { enabled: false }, fontFamily: "'JetBrains Mono', monospace" }}
          />
        </div>
        <div className="cs-output-panel" style={{ flex: 1 }}>
          <div className="cs-output-label">Output</div>
          {output}
        </div>
      </div>
    </div>
  );
}
