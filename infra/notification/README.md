# Notification Service – Infra

This directory contains a `docker-compose.yml` setup for running the **Notification Service** together with its required infrastructure:

* **PostgreSQL** – primary database
* **Kafka + Zookeeper** – message broker for notification events
* **Notification Service** – Quarkus application (built from local source)

---

## 🚀 How to run

### 1. Set your Novu API key

```bash
export NOVU_API_KEY="your-api-key"
```

### 2. Build the Notification Service Docker image

From inside `notification-service/`:

```bash
./mvnw package -DskipTests
docker build -f src/main/docker/Dockerfile.jvm -t notification-service .
```

### 3. Start all services

From the directory containing `docker-compose.yml`:

```bash
docker-compose up --build
```

### 4. Check that services are running

NotificationService:

```
http://localhost:8081/q/health
```

PostgreSQL:

```
localhost:5432
db: notification
```

Kafka:

```
localhost:9092
```

---

## 🧹 Cleanup

To stop everything:

```bash
docker-compose down
```

To also remove database data:

```bash
docker-compose down -v
```
