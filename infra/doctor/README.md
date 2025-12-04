# Doctor Service – Infra

This setup provides the Doctor Service, its PostgreSQL database.

## Services

### **postgres**

PostgreSQL database storing all Doctor Service data.

### **doctor-service**

Quarkus-based microservice providing doctor-related API functionality.

## Infrastructure

* **basit-network** — Shared Docker network for service communication.
* **postgres_data** - Volumes for persistent database.
