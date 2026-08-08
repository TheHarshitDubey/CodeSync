import axios from 'axios';

const API_BASE = 'https://codesync-backend-kzmj.onrender.com';

const client = axios.create({ baseURL: API_BASE });

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const register = (username, email, password) =>
  client.post('/auth/register', { username, email, password });

export const login = (username, password) =>
  client.post('/auth/login', { username, password });

export const createRoom = (name, language) =>
  client.post('/rooms', { name, language });

export const getRoom = (roomCode) =>
  client.get(`/rooms/${roomCode}`);

export const getDocument = (roomCode) =>
  client.get(`/rooms/${roomCode}/document`);

export const executeCode = (code, language, stdin) =>
  client.post('/execute', { code, language, stdin });

export default client;
