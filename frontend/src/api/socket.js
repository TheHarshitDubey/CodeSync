import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_URL = 'https://codesync-backend-kzmj.onrender.com/ws/collaboration';

/**
 * Creates a STOMP client connected over SockJS. Caller is responsible for
 * calling .activate() and .deactivate() to manage the connection lifecycle.
 */
export function createSocketClient({ onConnect, onEdit, onPresence, roomCode }) {
  const client = new Client({
    webSocketFactory: () => new SockJS(WS_URL),
    reconnectDelay: 3000,
    onConnect: () => {
      client.subscribe(`/topic/rooms/${roomCode}`, (message) => {
        onEdit(JSON.parse(message.body));
      });
      client.subscribe(`/topic/rooms/${roomCode}/presence`, (message) => {
        onPresence(JSON.parse(message.body));
      });
      onConnect();
    }
  });

  return client;
}

export function sendEdit(client, roomCode, username, content, clientVersion) {
  client.publish({
    destination: `/app/rooms/${roomCode}/edit`,
    body: JSON.stringify({ username, content, clientVersion })
  });
}

export function sendJoin(client, roomCode, username) {
  client.publish({
    destination: `/app/rooms/${roomCode}/join`,
    body: JSON.stringify({ username, type: 'JOIN' })
  });
}

export function sendLeave(client, roomCode, username) {
  client.publish({
    destination: `/app/rooms/${roomCode}/leave`,
    body: JSON.stringify({ username, type: 'LEAVE' })
  });
}
