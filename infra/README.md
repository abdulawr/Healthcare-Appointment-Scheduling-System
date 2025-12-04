# Infrastructure

This directory contains all infrastructure components for the Healthcare Appointment Scheduling System.  
Each submodule represents a standalone part of the platform (Identity, Notification stack, API Gateway), and all components communicate over a shared Docker network.

---

## Purpose

The infra layer provides:

- **Identity & Authentication** – Keycloak realm, clients, and OIDC setup.
- **API Gateway** – Kong + oauth2-proxy as the protected entrypoint to all backend services.
- **Service Infrastructure** – Databases, Kafka, and service containers for local development.
- **Local Developer Environment** – Unified startup using Docker Compose.

All modules are designed for local development and can be run independently or together.

---

## Structure

```

infra/
├── api-gateway/         # Kong + oauth2-proxy configuration and declarative routes
├── identity/            # Keycloak + Postgres with imported realm
├── doctor/              # DoctorService stack: service container + Postgres
└── notification/        # NotificationService stack: Kafka + Zookeeper + Postgres + service container

````

Each directory contains its own `docker-compose.<name>.yml` and README.

---

## Shared Docker Network

All stacks communicate using the same shared Docker network:

```bash
docker network create basit-network
````

Make sure this network exists **before starting** any compose stack.

---

## Environment Variables

Some services require secrets. Setup environment variables:

1. Create a real env file from the sample:

   ```bash 
   cp infra/.env.example infra/.env
   ```

2. Configure env variables:

   2.1. Generate OAUTH2_PROXY_COOKIE_SECRET: `openssl rand -base64 32 | tr -- "+/" "-_"`

   2.2. Get `OAUTH2_PROXY_CLIENT_SECRET` and `<service_name>_OIDC_CLIENT_SECRET` from the Keycloak Admin Panel.

   2.3. Get `NOVU_API_KEY` from the Novu admin panel.

   2.4. Generate `NOVU_HMAC_SECRET`: `openssl rand -base64 16 | tr -- "+/" "-_"`

3. Put all results to the `infra/.env`

---

## Startup Order

Although each stack can be run individually, the typical order is:

1. **Infra**

   ```bash
   cd infra
   ```

2. **Identity (Keycloak)**
   Provides OIDC endpoints required by oauth2-proxy and backend services.

   ```bash
   make identity-up
   ```

3. **Notification stack**
   Runs Postgres, Kafka, and NotificationService.

   ```bash
   make notification-up
   ```

4. **Doctor stack**
   Runs Postgres, Kafka, and DoctorService.

   ```bash
   make doctor-up
   ```

5**API Gateway**
   Routes public traffic and enforces authentication.

   ```bash
   make gateway-up
   ```

---

## Communication Overview

Inside the `basit-network`, services talk via hostnames:

* **Keycloak:** `identity-keycloak:8080`
* **NotificationService:** `notification-service:8080`
* **Kafka:** `notification-kafka:9092`
* **Notification DB:** `notification-postgres:5432`
* **DoctorService:** `doctor-service:8082`
* **Doctor DB:** `doctor-postgres:5433`
* **Kong:** `api-gateway:8000`
* **oauth2-proxy:** `oauth2-proxy:4180`

This ensures correct service discovery without using `localhost`.

---

## Useful Commands

### View logs

```bash
docker logs <container-name> -f
```

### Stop all containers in a stack

```bash
make stop-all
```

### Rebuild a service (e.g., NotificationService)

```bash
make notification-down
make notification-up
```

---

## Cleanup

Volumes may persist data across runs. To remove them:

```bash
docker volume prune
```

---

## Adding a New Service

To onboard a new microservice into the platform, follow these steps:

### 1. Create the service

Inside the root project, create a new folder:

```
<service-name>/
  src/main/...
  Dockerfile
  README.md
```

Build and run it locally first to confirm it works on port `8080` (or any port you choose).

### 2. Add it to docker-compose (infra)

Create a new compose file under `infra/<service-name>` and update `infra/Makefile`.

Minimal example:

```yaml
services:
  <service-name>:
    build:
      context: ../../<service-name>
    container_name: <service-name>
    environment:
      QUARKUS_OIDC_AUTH_SERVER_URL: ${OIDC_ISSUER_INTERNAL}
      QUARKUS_OIDC_CLIENT_ID: <service-name>
      QUARKUS_OIDC_TOKEN_AUDIENCE: api-gateway
    networks:
      - basit-network
```

> **Important:**
> Give the container a stable hostname (its service name). Other components will reference it as `http://<service-name>:8080`.

### 3. Register the service in Keycloak

In the Keycloak Admin Console:

1. Open realm **`basit-cz`**
2. Go to **Clients → Create**
3. Set:

   * **Client ID:** `<service-name>`
   * **Client type:** OpenID Connect
   * **Access Type:** Confidential
4. Generate a client secret and add it to `.env`:

```env
<UPPER_SNAKE>_OIDC_CLIENT_ID=<service-name>
<UPPER_SNAKE>_OIDC_CLIENT_SECRET=<generated-secret>
```

The service can now validate access tokens coming through the gateway.

### 4. Add routes in Kong

Edit `infra/api-gateway/kong/kong.yml`:

```yaml
services:
  - name: <service-name>-svc
    url: http://<service-name>:8080
    routes:
      - name: <service-name>-route
        paths:
          - /<service-name>
        strip_path: false
```

This exposes your service publicly under:

```
http://localhost:8000/<service-name>
```

Requests will be authenticated by **oauth2-proxy** before hitting your service.

### 5. (Optional) Make some endpoints public

If your service needs unauthenticated endpoints (webhooks, health checks…), update its `application.properties`:

```properties
quarkus.http.auth.permission.public.paths=/public/*
quarkus.http.auth.permission.public.policy=permit
```

And if needed, bypass oauth2-proxy in Kong by creating a separate service+route (as done for Novu webhooks).

### 6. Redeploy the gateway

After changing `kong.yml`, rebuild the API gateway:

```bash
cd infra/api-gateway
make identity-down
make identity-up
```

---

## Notes

* All configurations are optimized for **local development**, not production.
* Secrets are expected to be managed via environment variables during development.
* Realm imports and declarative gateway configs make the environment reproducible.
