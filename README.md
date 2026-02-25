# TaskFlow

Team task management with real-time notifications. Modular monolith on Spring Boot, deployed to AKS.

![Java](https://img.shields.io/badge/Java-24-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-green)
![React](https://img.shields.io/badge/React-18-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)
![Kubernetes](https://img.shields.io/badge/Kubernetes-AKS-326CE5)
![Terraform](https://img.shields.io/badge/Terraform-IaC-7B42BC)

## About

TaskFlow lets teams create, assign, and track tasks. When something changes, the assignee gets a WebSocket notification in real time. The backend is a modular monolith (Spring Modulith), deployed to Azure Kubernetes Service. All infrastructure is provisioned through Terraform.

## Features

- JWT login with Google OAuth2 sign-in, refresh tokens stored in HttpOnly cookies
- Task CRUD with assignment to team members
- WebSocket notifications on task changes
- Kafka for async events locally, Azure Service Bus in production
- Logging through ELK, tracing with Jaeger, metrics via Prometheus and Grafana
- Secrets injected at runtime from HashiCorp Vault (no env vars)

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | React, Axios, React Router, WebSocket |
| Backend | Spring Boot 4.0, Spring Modulith (Java 24) |
| Database | PostgreSQL |
| Cache | Redis (JWT blacklist + profile cache) |
| Messaging | Kafka (local) / Azure Service Bus (production) |
| Secrets | HashiCorp Vault + Vault Agent sidecar |
| Logging | Filebeat → Logstash → Elasticsearch → Kibana |
| Tracing | OpenTelemetry → Jaeger |
| Metrics | Prometheus → Grafana |
| IaC | Terraform (all Azure resources) |
| Cloud | Azure (AKS, ACR, Azure DB, Azure Cache, Key Vault) |
| CI/CD | GitHub Actions + ArgoCD (GitOps) |

## Architecture

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

## Getting started

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

# start infrastructure (PostgreSQL, Redis, Kafka)
docker compose up -d

# run the backend
cd backend
./mvnw spring-boot:run

# run the frontend (in a separate terminal)
cd frontend
npm install
npm run dev
```

The backend runs on `http://localhost:8080` and the frontend on `http://localhost:5173`.

## Project structure

```
TaskFlow/
├── backend/
│   ├── src/main/java/art/kafynextlevel/backend/
│   │   ├── auth/           # JWT, OAuth2, login/register
│   │   ├── user/           # user profiles and management
│   │   ├── task/           # task CRUD and assignment
│   │   ├── notification/   # WebSocket + Kafka consumers
│   │   ├── config/         # Security, Redis, Kafka, WebSocket configs
│   │   └── BackendApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── frontend/
│   └── (React app)
├── docker-compose.yml
└── README.md
```

## License

MIT
