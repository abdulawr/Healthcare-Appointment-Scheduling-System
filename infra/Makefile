# Makefile for infra/
#
# Usage examples:
#   make identity-up
#   make identity-down
#   make notification-up
#   make gateway-up
#   make start-all
#   make stop-all

INFRA_DIR := $(CURDIR)
ENV_FILE  := $(INFRA_DIR)/.env
NETWORK   := basit-network

COMPOSE_IDENTITY     := docker-compose --env-file $(ENV_FILE) -f identity/docker-compose.identity.yml
COMPOSE_NOTIFICATION := docker-compose --env-file $(ENV_FILE) -f notification/docker-compose.notification.yml
COMPOSE_GATEWAY      := docker-compose --env-file $(ENV_FILE) -f api-gateway/docker-compose.api-gateway.yml
COMPOSE_ANALYTICS    := docker-compose --env-file $(ENV_FILE) -f analytics/docker-compose.analytics.yml
COMPOSE_MONITORING   := docker-compose --env-file $(ENV_FILE) -f monitoring/docker-compose.monitoring.yml

.PHONY: help network-create network-remove \
        identity-up identity-down identity-logs \
        notification-up notification-down notification-logs \
        analytics-up analytics-down analytics-logs \
        gateway-up gateway-down gateway-logs \
        monitoring-up monitoring-down monitoring-logs \
        start-all stop-all logs-all

help:
	@echo "Available targets:"
	@echo "  network-create          Create shared Docker network ($(NETWORK))"
	@echo "  network-remove          Remove shared Docker network ($(NETWORK))"
	@echo
	@echo "  identity-up             Start Keycloak + DB"
	@echo "  identity-down           Stop Keycloak stack"
	@echo "  identity-logs           Follow Keycloak logs"
	@echo
	@echo "  notification-up         Start Notification stack (Postgres, Kafka, service)"
	@echo "  notification-down       Stop Notification stack"
	@echo "  notification-logs       Follow NotificationService logs"
	@echo
	@echo "  analytics-up            Start Analytics stack (Postgres, Kafka, service)"
	@echo "  analytics-down          Stop Analytics stack"
	@echo "  analytics-logs          Follow AnalyticsService logs"
	@echo
	@echo "  gateway-up              Start API Gateway (Kong + oauth2-proxy)"
	@echo "  gateway-down            Stop API Gateway"
	@echo "  gateway-logs            Follow Kong logs"
	@echo
	@echo "  monitoring-up           Start Monitoring stack"
	@echo "  monitoring-down         Stop Monitoring stack"
	@echo "  monitoring-logs         Follow Monitoring stack logs"
	@echo
	@echo "  start-all               Start identity, notification, and gateway"
	@echo "  stop-all                Stop all infra stacks"
	@echo "  logs-all                Tail logs for main services"

# --- Network management ---

network-create:
	docker network create $(NETWORK) || true

network-remove:
	docker network rm $(NETWORK) || true

# --- Identity (Keycloak) ---

identity-up: network-create
	$(COMPOSE_IDENTITY) up -d

identity-down:
	$(COMPOSE_IDENTITY) down

identity-logs:
	$(COMPOSE_IDENTITY) logs -f

# --- Notification stack ---

notification-up: network-create
	$(COMPOSE_NOTIFICATION) up -d

notification-down:
	$(COMPOSE_NOTIFICATION) down

notification-logs:
	$(COMPOSE_NOTIFICATION) logs -f notification-service

# --- Analytics stack ---

analytics-up: network-create
	$(COMPOSE_ANALYTICS) up -d

analytics-down:
	$(COMPOSE_ANALYTICS) down

analytics-logs:
	$(COMPOSE_ANALYTICS) logs -f analytics-service


# --- API Gateway (Kong + oauth2-proxy) ---

gateway-up: network-create
	$(COMPOSE_GATEWAY) up -d

gateway-down:
	$(COMPOSE_GATEWAY) down

gateway-logs:
	$(COMPOSE_GATEWAY) logs -f kong

# --- Monitoring ---

monitoring-up: network-create
	$(COMPOSE_MONITORING) up -d

monitoring-down:
	$(COMPOSE_MONITORING) down

monitoring-logs:
	$(COMPOSE_MONITORING) logs -f kong

# --- Convenience targets ---

start-all: identity-up notification-up analytics-up monitoring-up gateway-up

stop-all: gateway-down notification-down analytics-down monitoring-down identity-down

