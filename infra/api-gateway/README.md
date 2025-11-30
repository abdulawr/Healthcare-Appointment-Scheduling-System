# API Gateway (Kong + oauth2-proxy)

This directory contains the API Gateway stack for the Healthcare Appointment Scheduling System.  
It uses **Kong** as the gateway and **oauth2-proxy** for OIDC authentication against Keycloak.

## What it does

- Exposes a single public entrypoint on **`http://localhost:8000`**.
- Protects `/notifications` with Keycloak login via **oauth2-proxy**.
- Handles `/oauth2/*` (login/callback) paths for the auth flow.
- Forwards Novu webhooks `/notifications/callbacks/novu` directly to `notification-service` (HMAC-protected in Kong).

## Running the gateway

Kong will be available at:

* **Gateway**: `http://localhost:8000`
* **Admin API (dev only)**: `http://localhost:8001`

You can then call the NotificationService via:

```bash
curl -v http://localhost:8000/notifications
```

(You’ll be redirected through the Keycloak login flow.)
