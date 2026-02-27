# TaskFlow

Team task management with real-time notifications. Modular monolith on Spring Boot, deployed to AKS.

![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-green)
![React](https://img.shields.io/badge/React-18_(planned)-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)
![Kubernetes](https://img.shields.io/badge/Kubernetes-AKS-326CE5)
![Terraform](https://img.shields.io/badge/Terraform-IaC-7B42BC)

## About

TaskFlow is a work-in-progress team task management platform. The current repo contains the backend skeleton (Spring Boot + Spring Modulith), Flyway migrations, and tests. Frontend, messaging, and infrastructure are planned and described below.

**Status (2026-02-26):**
- Backend skeleton only: modules, User entity/repo, Flyway migrations, tests
- Frontend, Docker Compose, messaging, observability, and cloud infra are planned

## Features

**Current**
- Spring Modulith module boundaries
- User entity + repository
- Flyway migrations for users/tasks + indexes
- Modulith, Flyway, and JPA tests

**Planned**
- JWT login with Google OAuth2 sign-in, refresh tokens stored in HttpOnly cookies
- Task CRUD with assignment to team members
- WebSocket notifications on task changes
- Kafka for async events locally, Azure Service Bus in production
- Logging through ELK, tracing with Jaeger, metrics via Prometheus and Grafana
- Secrets injected at runtime from HashiCorp Vault (no env vars)

## Tech stack

**Current**
- Backend: Spring Boot 4.0.3, Spring Modulith 2.0.3, Java 25
- Database: PostgreSQL (Flyway migrations)

**Planned**
- Frontend: React, Axios, React Router, WebSocket
- Cache: Redis (JWT blacklist + profile cache)
- Messaging: Kafka (local) / Azure Service Bus (production)
- Secrets: HashiCorp Vault + Vault Agent sidecar
- Logging: Filebeat → Logstash → Elasticsearch → Kibana
- Tracing: OpenTelemetry → Jaeger
- Metrics: Prometheus → Grafana
- IaC: Terraform (all Azure resources)
- Cloud: Azure (AKS, ACR, Azure DB, Azure Cache, Key Vault)
- CI/CD: GitHub Actions + ArgoCD (GitOps)

## Architecture (Target)

```
┌──────────┐       ┌─────────────────────────────────────┐       ┌────────────┐
│          │       │          Spring Boot Backend         │       │            │
│  React   │──────▶│                                     │──────▶│ PostgreSQL │
│  (SPA)   │◀──────│  ┌──────┐ ┌──────┐ ┌────────────┐  │       │            │
│          │       │  │ Auth │ │ Task │ │Notification│  │       └────────────┘
└──────────┘       │  └──────┘ └──────┘ └────────────┘  │
                   │         Spring Modulith             │       ┌────────────┐
                   │                                     │──────▶│   Redis    │
                   └──────────────┬──────────────────────┘       └────────────┘
                                  │
                                  ▼
                   ┌──────────────────────────────┐
                   │   Kafka / Azure Service Bus   │
                   └──────────────────────────────┘
```

The auth, user, task, and notification modules each have their own package. Spring Modulith enforces the boundaries at compile time, so cross-module calls go through public APIs only.

Observability runs alongside the app on Kubernetes:

```
App pods  ──▶  Filebeat  ──▶  Logstash  ──▶  Elasticsearch  ──▶  Kibana
    │
    ├── OpenTelemetry SDK  ──▶  Jaeger
    │
    └── Micrometer  ──▶  Prometheus  ──▶  Grafana
```

## Getting started (Backend only)

### Prerequisites

- Java 24
- Maven 3.9+ (or use the included `mvnw` wrapper)
- Docker and Docker Compose
- Node.js 20+

### Run locally

```bash
# clone the repo
git clone https://github.com/<org>/TaskFlow.git
cd TaskFlow

# ensure PostgreSQL is running on localhost:5432
# default credentials are DB_USERNAME=taskflow, DB_PASSWORD=taskflow
cd backend
./mvnw spring-boot:run
```

The backend runs on `http://localhost:8080`.

## Project structure

```
TaskFlow/
├── backend/
│   ├── src/main/java/art/kafynextlevel/taskflow/
│   │   ├── auth/           # module package (package-info only for now)
│   │   ├── user/           # User entity + repository
│   │   ├── task/           # module package (package-info only for now)
│   │   ├── notification/   # module package (package-info only for now)
│   │   ├── config/         # Security config + stubs
│   │   └── TaskflowApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── frontend/               # planned
├── docker-compose.yml      # planned
└── README.md
```

## License

MIT
