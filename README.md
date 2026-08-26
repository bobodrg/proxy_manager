# Proxy Manager

A self-hosted reverse proxy with a web admin UI, inspired by [Nginx Proxy Manager](https://nginxproxymanager.com/) — but the proxy engine itself is Java (Spring Cloud Gateway), not an externally-controlled Nginx.

You configure domains and their target upstreams through a React admin UI (protected by JWT login), backed by a REST API. Configuration is stored in Postgres and read live by the Gateway — adding, editing, or disabling a host takes effect immediately, with no restart.

## How it works

```
Browser ──► React Admin UI ──► Spring Boot Admin API (JWT-protected)
                                        │
                                        ▼
                                   PostgreSQL
                                        ▲
                                        │ (reads active hosts, refreshed live)
External traffic ──► Spring Cloud Gateway ──► your upstream server(s)
```

Both the admin API and the Gateway run inside the **same** Spring Boot process (it has to be — Spring Cloud Gateway requires the reactive WebFlux stack, so the whole app is reactive). Gateway routes are matched by the incoming request's `Host` header against the domains configured in the `proxy_host` table; everything else (the admin API, the login endpoint, the admin UI's own static files) is matched by path and is unaffected by whatever routes exist.

## Tech stack

- **Backend:** Java 21, Spring Boot 3.3, Spring Cloud Gateway, Spring WebFlux, Spring Data JPA, Spring Security + JJWT, PostgreSQL
- **Frontend:** React 19, Vite, axios, react-router-dom
- **Database:** PostgreSQL 16
- **Containerization:** Docker, Docker Compose (multi-stage build: the React app is built and embedded into the Spring Boot jar's static resources — one image, one container, no separate web server)

## Running it (Docker Compose)

This is the intended way to run the whole thing:

```bash
docker compose up --build
```

Then open **http://localhost:8080**. Log in with the default admin account:

- Username: `admin`
- Password: `admin123`

(These — and the JWT signing secret — are set as plain environment variables in `docker-compose.yml` for convenience. Change them before running this anywhere other than your own machine; see `app.jwt.secret` / `app.admin.default-*` in `backend/src/main/resources/application.yml` for what they control.)

The admin user is created automatically on first startup if none exists yet. Postgres data persists in a named Docker volume (`db-data`) across restarts; `docker compose down -v` wipes it if you want a truly clean slate.

### Trying out the proxy feature

1. Log in to the admin UI and add a host — e.g. domain `myapp.local`, target URL pointing at something you have running. If that something runs on your own machine (not in a container), use `http://host.docker.internal:<port>` as the target, not `localhost` — from inside the `app` container, `localhost` means the container itself.
2. Send a request with that Host header:
   ```bash
   curl -H "Host: myapp.local" http://localhost:8080/
   ```
3. Edit the host (or toggle it inactive) in the UI and repeat the `curl` — the change is live immediately, no restart.

## Running it for local development (no Docker)

Useful when actively working on the code — faster iteration than rebuilding the Docker image each time.

**Database** (only needed once, keep it running in the background):
```bash
docker run -d --name proxy-manager-dev-db \
  -e POSTGRES_DB=proxy_manager -e POSTGRES_USER=proxy_manager -e POSTGRES_PASSWORD=proxy_manager \
  -p 5434:5432 postgres:16-alpine
```

**Backend** (uses `localhost:5434` by default, see `backend/src/main/resources/application.yml`):
```bash
cd backend
mvn spring-boot:run
```

**Frontend** (Vite dev server on :5173, proxies `/admin` and `/auth` to the backend on :8080 — see `frontend/vite.config.js`):
```bash
cd frontend
npm install
npm run dev
```

## API reference

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/auth/login` | none | Exchange username/password for a JWT |
| `GET` | `/admin/hosts` | Bearer JWT | List all proxy hosts |
| `GET` | `/admin/hosts/{id}` | Bearer JWT | Get one proxy host |
| `POST` | `/admin/hosts` | Bearer JWT | Create a proxy host |
| `PUT` | `/admin/hosts/{id}` | Bearer JWT | Update a proxy host |
| `DELETE` | `/admin/hosts/{id}` | Bearer JWT | Delete a proxy host |
| `POST` | `/actuator/gateway/refresh` | none | Force the Gateway to reload routes (not needed in normal use — the admin API already triggers this after every write) |

`ProxyHost` shape: `{ id, domeniu, targetUrl, activ }` — `domeniu` is the Host header to match, `targetUrl` is where matching traffic gets forwarded, `activ` toggles whether the route is live.

## Project structure

```
backend/    Spring Boot app: Gateway + Admin API + Auth (single module, single deployable jar)
frontend/   React admin UI (Vite)
Dockerfile  Multi-stage build: frontend build -> backend build (embeds frontend as static resources) -> runtime image
docker-compose.yml   app + db services
```

## Known limitations

This was built as a learning project / portfolio piece, not for production use at scale:

- Single admin account, no user management, no roles/permissions.
- JWTs aren't revocable — logout is client-side only; a leaked token stays valid until it expires.
- Any request whose `Host` header doesn't match a configured proxy domain (and isn't `/admin/**` or `/auth/**`) falls through to the admin UI itself rather than a plain 404 — acceptable for a single-operator tool, but worth knowing.
- `spring.jpa.hibernate.ddl-auto=update` manages the schema — a real deployment would use versioned migrations (Flyway/Liquibase) instead.

## Screenshots

_(added manually)_

## License

MIT — see [LICENSE](LICENSE).
