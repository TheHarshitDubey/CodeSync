# CodeSync — Real-Time Collaborative Code Editor

A real-time collaborative code editor where multiple users can write and run
code together in a shared room. Built as a single Spring Boot backend
(monolith, not microservices) with a React + Monaco frontend.

## Features

- JWT-based registration/login
- Create or join a room via a short shareable room code
- Real-time collaborative editing over WebSocket (STOMP)
- Live presence — see who else is in the room
- Run code (Java, Python, JavaScript) via the Judge0 API and see output

## Tech Stack

- **Backend:** Java 17, Spring Boot 3, Spring Security, Spring Data JPA, Spring WebSocket (STOMP)
- **Database:** PostgreSQL (users, rooms — durable data)
- **Cache/live state:** Redis (document snapshot, presence — ephemeral data)
- **Code execution:** Judge0 CE API
- **Frontend:** React, Monaco Editor, StompJS, Vite

## Architecture

Unlike a multi-service setup, this is intentionally a **single Spring Boot
app** with logically separated packages:

```
com.codesync
├── auth/           # register, login, JWT issuance
├── room/           # room creation/lookup
├── collaboration/  # WebSocket sync, presence, document store (Redis)
├── execution/      # Judge0 client
└── config/         # security, JWT filter, WebSocket, Redis config
```

This keeps local setup and deployment simple for a solo project while still
demonstrating the same core distributed-systems concepts (real-time sync,
durable vs. ephemeral storage split, external execution service) that a
microservices version would.

### Design decisions

**Durable vs. ephemeral storage split**
PostgreSQL stores user accounts and room metadata (durable, low write
frequency). Redis stores the live document content, its version counter, and
room presence (high write frequency, doesn't need to survive a restart of
the app). This keeps the hot path — every keystroke — off the relational
database.

**Conflict resolution: last-write-wins with versioning (v1)**
The client sends the full updated document on every change (debounced
client-side). The server stamps each update with a monotonically
increasing version number and broadcasts it to all clients, so everyone
converges on the same state. This is a deliberate, documented trade-off —
fine for small rooms (2-5 people) with low-latency connections, but it can
overwrite a concurrent edit under real contention.

> **Next step:** replace this with Operational Transformation or a CRDT
> (e.g. Yjs-style) so concurrent edits merge instead of overwrite. This is
> the natural v2 — the version-counter design already in place is what a
> proper OT/CRDT layer would build on top of.

**Code execution via Judge0, not a custom Docker sandbox**
Running untrusted user code safely (resource limits, filesystem isolation,
timeouts) is a hard, security-sensitive problem on its own. Judge0 already
solves it well via a hosted API. Building and hardening a custom Docker
execution service is a reasonable v2 if there's time.

## Local Setup

### Prerequisites

- Java 17+ (JDK)
- Maven (or use IntelliJ's bundled Maven)
- Node.js 18+
- Docker (for Postgres + Redis via Compose) — or install both natively
- A free Judge0 API key from [RapidAPI](https://rapidapi.com/judge0-official/api/judge0-ce)

### 1. Start Postgres + Redis

```bash
cd infra
docker compose up -d
```

### 2. Configure the backend

Copy `.env.example` to `.env` (or just set these as environment variables /
IntelliJ run config values) and fill in:

- `JWT_SECRET` — any long random string
- `JUDGE0_API_KEY` — your RapidAPI key

### 3. Run the backend

See "Running in IntelliJ" below, or from the command line:

```bash
cd backend
mvn spring-boot:run
```

Backend runs on `http://localhost:8080`.

### 4. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:3000`.

## Next Steps / Future Work

- Replace last-write-wins with OT/CRDT for real concurrent-edit merging
- Move code execution to a self-hosted Docker sandbox for more control
- Add refresh tokens + token revocation
- Persist room chat if long-lived sessions are needed
- Split into microservices (auth / collaboration / execution) if scaling demands it
