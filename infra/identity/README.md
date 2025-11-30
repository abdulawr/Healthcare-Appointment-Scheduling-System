# **Identity Service (Keycloak) – README**

This directory contains the full **Identity Service** setup for the Healthcare Appointment System.
It provides:

* **Authentication & Authorization** (OIDC / OAuth2)
* **Token issuance** for backend services
* **Realm configuration** (clients, roles, scopes)
* **Local development environment** (Keycloak + Postgres)

All developers get the *same exact Keycloak configuration* automatically via **realm import**.

---

# 1. **Overview**

Our Identity Service uses **Keycloak**, running inside Docker.
It loads all configuration automatically from:

```
infra/identity/keycloak/basit-cz-realm.json
```

This JSON file contains:

* The **realm**: `basit-cz`
* All **clients** (e.g. `notification-service`)
* All **roles**, **groups**, **protocol mappers**
* Optional **test users** (if included)

Developers do **not** have to manually configure anything in Keycloak.

---

# 2. **Getting Started**

### **Start Identity Service + its Postgres**

From the project root:

```bash
docker compose -f infra/identity/docker-compose.identity.yml up -d;
```

This starts:

* Postgres for identity service
* Keycloak
* Automatically imports `basit-cz` realm

### **Access Keycloak**

* Admin Console:
  **[http://localhost:8080](http://localhost:8080)**
* Health:
* **[http://localhost:9000/health](http://localhost:9000/health)**
* Credentials (dev only):

  ```
  admin / admin
  ```

### **Realm Discovery Endpoint**

```
http://localhost:8080/realms/basit-cz/.well-known/openid-configuration
```

This is used by Quarkus services to validate tokens.

---

# 3. **Realm Import Explained**

Keycloak loads the realm from file on startup.

Docker Compose mounts this directory:

```
infra/identity/keycloak/  →  /opt/keycloak/data/import
```

And Keycloak starts with:

```
start-dev --import-realm
```

Meaning:

* On first run → imports the entire realm
* On following runs → detects it already exists (no duplicates)

If you modify the realm file, remove the Keycloak DB volume:

```bash
docker compose -f infra/identity/docker-compose.identity.yml down -v
docker compose -f infra/identity/docker-compose.identity.yml up -d
```

This forces a fresh import.

---

# 4. **Clients & Secrets**

### **Client ID is NOT a secret**

Safe to commit:

```
notification-service
frontend-app
```

### **Client Secret IS secret**

For dev only you may include it in the JSON,
but services should still read it from **environment variables**:

Example for `notification-service`:

```bash
export NOTIFICATION_OIDC_CLIENT_SECRET=<client-secret>
```

Or in Docker Compose:

```yaml
environment:
  QUARKUS_OIDC_CREDENTIALS_SECRET: ${NOTIFICATION_OIDC_SECRET}
```

Place secrets in a `.env` file that is **gitignored** or in the Intellij IDEA configuration.

---

# 5. **New Service Configuration**

### **Dev mode (`quarkus:dev`)**

When running directly on your host, Keycloak is reachable via `localhost`.

Add this to `new-service/src/main/resources/application.properties`:

```properties
%dev.quarkus.oidc.auth-server-url=http://localhost:8080/realms/basit-cz
%dev.quarkus.oidc.client-id=new-service
%dev.quarkus.oidc.credentials.secret=${NEW_OIDC_CLIENT_SECRET:}
%dev.quarkus.oidc.application-type=service
%dev.quarkus.oidc.roles.role-claim-path=realm_access/roles
```

Run a new service:

```bash
export NEW_OIDC_CLIENT_SECRET=<secret>
./mvnw quarkus:dev
```

---

# 6. **Getting an Access Token (Client Credentials)**

This is useful for testing protected endpoints.

```bash
TOKEN=$(curl -s \
  -d "client_id=notification-service" \
  -d "client_secret=$NOTIFICATION_OIDC_CLIENT_SECRET" \
  -d "grant_type=client_credentials" \
  http://localhost:8080/realms/basit-cz/protocol/openid-connect/token \
  | jq -r .access_token)
```

Then call your service:

```bash
curl -i -X POST -H "Authorization: Bearer $TOKEN" \
     http://localhost:8000/notifications \
    -H "Content-Type: application/json" \
    -d '{
      "idempotencyKey": "order-123-user-42-email",
      "userId": "user-42",
      "eventType": "order.shipped",
      "locale": "en-US",
      "brand": "default",
      "channels": ["EMAIL"],
      "payload": {
        "orderId": "123",
        "trackingUrl": "https://example.com/track/123"
      }
    }'
```

---

# 7. **Updating the Realm Configuration**

If you make changes in Admin Console (add roles, clients, mappers, etc.):

### **1. Export updated realm**

```
Realm settings → Export → Export current realm
```

### **2. Replace the file**

```
infra/identity/keycloak/basit-cz-realm.json
```

### **3. Re-import**

```bash
docker compose -f infra/identity/docker-compose.identity.ym down -v
docker compose -f infra/identity/docker-compose.identity.ym up -d
```

This ensures all developers get the exact same config.

---

# 8. **Troubleshooting**

### **“UnknownHostException: keycloak” during quarkus:dev**

You’re running Quarkus *outside* Docker → use `localhost:8081` in `%dev.*` config.

### **Keycloak refuses to start after changing the realm**

Remove old volumes:

```bash
docker compose  down -v
docker compose up -d
```

### **Token doesn’t contain roles**

Make sure:

* Roles are assigned to the **service account**
* You included protocol mapper “Realm roles to access token”

### **Service returns 401**

Usually the token was issued for the wrong client or wrong realm.

---

# 9. Summary

* Identity Service uses **Keycloak + Postgres**, managed through Docker.
* Realm configuration is stored in Git and automatically imported.
* No developer needs to manually configure clients/roles.
* Services use environment variables to load client secrets.
* Dev and Docker configs are separated cleanly.
