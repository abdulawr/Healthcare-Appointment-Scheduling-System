# 📬 Notification Service

A standalone microservice responsible for receiving notification events, applying routing and idempotency logic, persisting notification history, and dispatching messages to **Novu** for multi-channel delivery.  
Built with **Java 17 + Quarkus**, **PostgreSQL**, **Flyway**, and optional **Kafka**.

---

## ✨ Features

- Receive notification events via **REST** (and optionally **Kafka**).
- Enforce **idempotency** via `idempotencyKey`.
- Map event types to **Novu workflows**.
- Compose and send notifications to **Novu** (`/v1/events/trigger`).
- Store **notification history** in PostgreSQL.
- Track basic delivery status (`PENDING`, `SENT`, `FAILED`).
- Ready for multi-channel routing, quiet hours, and preferences.

---

## 🏗 Architecture

### High-level flow

```mermaid
flowchart LR
    subgraph Upstream
      SVC1[Calendar Service]
      SVC2[People Service]
    end

    SVC1 -->|REST /notifications| NS[(Notification Service)]
    SVC2 -->|REST /notifications| NS

    subgraph Infra
      DB[(PostgreSQL<br/>notification table)]
      K[(Kafka)]
    end

    NS -->|JPA / Flyway| DB
    K -->|notification-events| NS

    NS -->|REST Client| NOVU[Novu API<br/>/v1/events/trigger]

    NOVU --> PROVIDERS[(Email / SMS / Push / Chat)]
````

**Main responsibilities of Notification Service:**

1. **Receive events** from internal services (`POST /notifications` or Kafka topic).
2. **Create notification record** in DB (with idempotency).
3. **Resolve workflow** (domain `eventType` → Novu Trigger Identifier).
4. **Call Novu** with `name`, `to`, `payload`, `transactionId`.
5. **Update status** based on Novu response.
6. **Expose APIs** to query notifications by id or by user.

---

## 📦 Tech Stack

| Component         | Technology                          |
| ----------------- | ----------------------------------- |
| Language          | Java 17                             |
| Framework         | Quarkus (RESTEasy Reactive, CDI)    |
| Database          | PostgreSQL                          |
| Migrations        | Flyway                              |
| Messaging         | Kafka (SmallRye Reactive Messaging) |
| External Provider | Novu                                |
| JSON              | Jackson                             |
| Build             | Maven                               |

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone <YOUR_REPO_URL>.git
cd notification-service
```

### 2. Start infrastructure (PostgreSQL, Kafka optional)

If you have a `docker-compose.yml` at repo root:

```bash
docker compose up -d
```

At minimum you need **PostgreSQL** running with:

* DB: `notification`
* User: `notification`
* Password: `notification`
* Port: `5432`

### 3. Configure Novu API key

In Novu Console:

* Go to **Project Settings → API Keys**
* Copy the **Secret Key** (not the Application Identifier)

Set it as environment variable:

```bash
export NOVU_API_KEY="xxxxxxxxxxxxxxx"
```

(or set it in your IDE run configuration)

### 4. Application configuration

Key configuration is in `src/main/resources/application.properties`.

You should export `NOTIFICATION_OIDC_CLIENT_SECRET` stored in your Keycloak Realm

### 5. Run in dev mode

```bash
./mvnw quarkus:dev
```

Useful URLs:

* Dev UI: `http://localhost:8080/q/dev`
* OpenAPI: `http://localhost:8080/q/openapi`

---

## 📤 Sending a Test Notification

```bash
curl -X POST http://localhost:8080/notifications \
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

Expected behavior:

* A new row is inserted into `notification` table.
* The service calls Novu’s `/v1/events/trigger`.
* Status is updated to `SENT` or `FAILED`.

---

## 🔁 Event-to-Workflow Mapping

The service maps domain event types (`eventType`) to Novu workflows via configuration.

Example:

```properties
novu.workflow.order-shipped=order-shipped-workflow
```

In code (simplified):

```java
if ("order.shipped".equals(eventType)) {
    return orderShippedWorkflow; // must match the Trigger Identifier in Novu
}
```

In the Novu UI, your workflow must have **Trigger Identifier** equal to `order-shipped-workflow` in this example.

---

## 🗄 Database Schema

Managed by **Flyway** migrations under:

```text
src/main/resources/db/migration
```

---

## 🔧 Development Notes

* **UUIDs**: generated per notification (e.g. `UuidCreator.getTimeOrderedEpoch()` or UUIDv7).
* **Idempotency**: controlled by `idempotencyKey` (unique index).
* **Transactions**: `createAndSend(...)` is the main `@Transactional` method — it:

    * inserts the notification,
    * calls Novu,
    * updates status and transactionId in a single transaction.

---

## 🧪 Testing

Run unit tests:

```bash
./mvnw test
```

---

## 🐞 Common Issues

### 401 Unauthorized from Novu

* Wrong or missing `NOVU_API_KEY`.
* Must use **Secret Key** from Novu → `Authorization: ApiKey <SECRET>`.

### 422 workflow_not_found

* The `name` field you send to Novu (workflowName) does not match any Trigger Identifier in Novu.
* Check `novu.workflow.*` properties and Novu UI.

### acknowledged=false; status=null

* Usually means response DTO doesn’t match Novu’s current JSON.
* Make sure `NovuTriggerResponse` matches the real response (flat or `{data: {...}}`).
