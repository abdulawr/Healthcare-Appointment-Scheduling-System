# IdentityService

- **Status**: `In Development`
- **Tier Level:** `Tier 1`

---

## Overview

We chose **Keycloak** because it provides a robust, standards-compliant, and 
feature-rich identity platform that covers authentication, authorization, federation, security, and multi-tenancy 
needs out of the box, while remaining open-source, self-hostable, and well-integrated with modern Java/Quarkus architectures.

### Key Responsibilities:

* **Standards compliance & discovery**: Implement OAuth2.0/OIDC flows (Auth Code + PKCE, Client Credentials, Refresh, Device Code), `.well-known/openid-configuration`, and **JWKS** for key rotation.
* **Token services**: Issue/validate **access**, **ID**, and **refresh** tokens; configurable lifetimes, signing/algorithms, introspection, revocation/blacklist, and audience/scope management.
* **Client & scope management**: Register clients, rotate secrets, assign grants/redirect URIs, enforce scopes, consent screens, and dynamic client registration (optional).
* **Authentication & SSO**: Username/password, **MFA** (TOTP/SMS/WebAuthn), passwordless, session management (SSO), step-up auth, risk/adaptive policies, lockout/rate-limit.
* **User directory & lifecycle**: User CRUD, attributes, groups/roles, password policies, recovery flows, and **SCIM** provisioning (in/out) for HR/CRM systems.
* **Federation & social login**: Act as SP/IdP with SAML/OIDC federation; external IdPs (Google, Microsoft, GitHub, LDAP/AD).
* **Authorization & claims**: RBAC/ABAC, claim mapping/transform, per-tenant/realm policies, fine-grained consent, and claims in ID/Access tokens.
* **Logout**: Front-channel/back-channel logout, global session termination, device/session lists.
* **Security & keys**: Secure key management/HSM/KMS, automated key rotation, TLS/mTLS, CSP/CORS hardening, audit trails for admin actions.
* **Tenanting & branding**: Multi-tenant realms/projects, per-tenant policies, themes/branding, locale/time-zone awareness.
* **Observability & ops**: Admin UI/APIs, audit/event logs, metrics/tracing, health checks, backups, HA/geo-replication/DR.
* **Compliance & privacy**: GDPR/PII controls, data retention/erasure, consent records, DPA support.
* **Developer experience**: SDKs/examples, test tenants, sandbox keys, clear error codes, and first-class docs.

---

## Implementation

### Standards compliance & discovery

Keycloak is a first-class **OIDC/OAuth 2.0** provider (and SAML), exposes `.well-known/openid-configuration`, **JWKS** for key discovery/rotation, and supports all mainstream flows (Auth Code + PKCE, Client Credentials, Device Code, Refresh), so your apps and services can integrate using pure standards with no vendor quirks.

### Token services

It issues **access**, **ID**, and **refresh** tokens with configurable lifetimes, audiences and scopes; supports **token introspection**, **revocation**, and per-client signing algos (RSA/ECDSA/EdDSA). Built-in rotation of realm keys makes crypto hygiene practical without downtime.

### Client & scope management

Through the Admin Console and REST API you can register clients, manage secrets/certs, define redirect URIs, **confidential/public/bearer-only** modes, consent screens, and custom **scopes**/mappers—giving you fine-grained control over what each client can request and receive.

### Authentication & SSO

Keycloak provides username/password, **MFA** (TOTP/SMS/email OTP), **WebAuthn/passkeys**, passwordless, step-up auth, brute-force protection, and session management for single sign-on across apps—so you cover both security and UX without extra products.

### User directory & lifecycle

It ships with a user store (with groups/roles, attributes, policies, self-service flows) and **user federation** for LDAP/Active Directory. For provisioning APIs (e.g., SCIM), there are mature community providers—so you can keep HR/IT systems in sync when needed.

### Federation & social login

As an IdP/Broker, Keycloak can **federate** to external IdPs via **OIDC/SAML** and supports one-click **social login** (Google, Microsoft, GitHub, etc.), letting you centralize auth while meeting B2B, B2E, and B2C scenarios.

### Authorization & claims

With **Authorization Services** (UMA 2.0, resource-based permissions, policy enforcers) plus powerful **claim mappers**, you can implement RBAC/ABAC and inject the right roles/attributes into tokens—keeping authorization logic consistent across services.

### Logout

Supports OIDC **RP-initiated**, front-channel and back-channel logout (plus SAML logout), and global session invalidation—so users can sign out everywhere and admins can terminate compromised sessions centrally.

### Security & keys

Backed by Quarkus, Keycloak offers TLS/mTLS options, **automated key rotation**, per-realm keystores, FIPS builds, and can integrate with **HSM/KMS/PKCS#11** via Java keystore providers—meeting stringent crypto and ops requirements.

### Tenanting & branding

**Realms** give true multi-tenancy (isolation of users, clients, keys, policies). **Themes** enable per-tenant branding/localization for login, emails, and account consoles—ideal for white-label products.

### Observability & ops

Admin UI + Admin REST, **audit/admin events**, **health/readiness** endpoints, **Prometheus metrics** (built-in/extension), backups/export, and HA clustering—so platform teams can run it reliably and monitor what matters.

### Compliance & privacy

Features for consent, T&C, email verification, password policies, account management, **data export/delete** help with GDPR/PII obligations; admin/audit events provide traceability for changes and access.

### Developer experience

Great docs, Docker images, Helm charts, rich **Admin REST API**, test realms, and seamless framework integration (e.g., **Quarkus `quarkus-oidc`**, Spring Security, Node/OIDC libs) let teams prototype fast and move to production with minimal glue code.

