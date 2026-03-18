# LogSphere

**LogSphere** is a professional-grade **AI-powered log analysis platform** built with **Java Spring Boot** for high-performance log ingestion, parsing, search, analytics, and anomaly detection.

It is designed to process logs from multiple sources concurrently using **multithreading and concurrency**, store structured results efficiently, and provide actionable operational insights through scalable REST APIs.

---

## Overview

Modern applications generate a huge volume of logs across services, environments, and infrastructure. Manually reading and analyzing those logs is slow, error-prone, and inefficient.

**LogSphere** solves this by providing a centralized backend system that can:

- Ingest logs from external sources and APIs
- Parse raw logs into structured data
- Process multiple logs concurrently
- Detect errors, repeated failures, and suspicious patterns
- Generate summaries and insights
- Support alerting and anomaly detection workflows

---

## Key Features

- **Concurrent log ingestion** — Handles multiple log streams and batches using multithreading
- **Structured log parsing** — Supports parsing raw, JSON, and application-style logs into normalized objects
- **REST API driven architecture** — Exposes APIs for ingestion, analysis, search, and monitoring
- **Open-source log source integration** — Integration with **Grafana Loki** HTTP API
- **High-performance backend** — Uses **Spring Boot**, **Executor-based concurrency**, **Redis**, and **PostgreSQL**
- **Search and analysis** — Filter logs by severity, source, timestamp, trace ID, and service
- **Spike detection** — Compares current error rates against previous windows to detect anomalies
- **Alert system** — Configurable alert rules with automatic evaluation

---

## Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| Java 17+ | Language |
| Spring Boot 4.x | Application framework |
| Spring Web | REST APIs |
| Spring Data JPA | ORM / Database access |
| Spring Validation | Request validation |
| Spring Boot Actuator | Health checks and metrics |

### Database & Caching
| Technology | Purpose |
|---|---|
| PostgreSQL 17 | Persistent storage for logs and analysis results |
| Redis 7 | Caching, job coordination, and performance optimization |

### Development Tools
| Technology | Purpose |
|---|---|
| Maven | Build tool |
| Lombok | Boilerplate reduction |
| Docker Compose | Local infrastructure |
| Spring Boot DevTools | Hot reload |

---

## Architecture

```text
Log Source / API / File Upload
            |
            v
    Log Ingestion Layer
     (4 ingestion threads)
            |
            v
      Parsing Pipeline
     (8 parser threads)
     JSON | Regex | StackTrace
            |
            v
  Concurrent Processing Engine
     (8 analysis threads)
     CompletableFuture pipeline
            |
            v
  Analysis + Aggregation Layer
   Summary | Spikes | Alerts
            |
            v
 PostgreSQL / Redis Storage
     (2 persistence threads)
            |
            v
        REST API Layer
```

### Core Layers

| Layer | Responsibility |
|---|---|
| Ingestion | Receives logs from uploads, Loki API, webhooks |
| Parsing | Converts raw logs to structured entities using strategy pattern |
| Processing | Concurrent batch processing with CompletableFuture |
| Analysis | Summary stats, spike detection, alert evaluation |
| Persistence | Raw + parsed log storage, analysis results |
| API | REST endpoints for upload, search, analysis, alerts |
| Scheduler | Periodic analysis, alert evaluation, spike detection |

---

## API Endpoints

### Ingestion APIs
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/logs/upload` | Upload log content for parsing |
| POST | `/api/logs/fetch/loki` | Fetch logs from Grafana Loki |

### Search APIs
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/logs/search` | Search parsed logs with filters |
| GET | `/api/logs/{id}` | Get a specific parsed log event |

### Analysis APIs
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/analysis/summary` | Get analysis summary for a time window |
| GET | `/api/analysis/errors/top` | Get top exception types |
| GET | `/api/analysis/spikes` | Detect error spikes |
| GET | `/api/analysis/exceptions` | Get repeated error messages |

### Alert APIs
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/alerts/rules` | Create an alert rule |
| GET | `/api/alerts/rules` | List all alert rules |
| DELETE | `/api/alerts/rules/{id}` | Delete an alert rule |
| GET | `/api/alerts` | Get active alert events |
| POST | `/api/alerts/test` | Manually trigger alert evaluation |

### Monitoring APIs
| Method | Endpoint | Description |
|---|---|---|
| GET | `/actuator/health` | Application health check |
| GET | `/actuator/info` | Application info |
| GET | `/actuator/metrics` | Application metrics |

---

## Concurrency Design

LogSphere uses a multi-stage concurrent pipeline:

| Stage | Thread Pool | Core / Max Threads | Queue |
|---|---|---|---|
| Ingestion | `ingestion-*` | 4 / 8 | 500 |
| Parsing | `parsing-*` | 8 / 16 | 1000 |
| Analysis | `analysis-*` | 8 / 16 | 500 |
| Persistence | `persistence-*` | 2 / 4 | 200 |

Key patterns used:
- `ThreadPoolTaskExecutor` for managed thread pools
- `CompletableFuture` for async pipeline processing
- Batch partitioning (500 logs per batch)
- Thread-safe aggregation
- Bounded queues for backpressure

---

## Database Schema

| Table | Purpose |
|---|---|
| `log_sources` | Registered log sources (Loki, File, Kafka, Webhook) |
| `raw_log_events` | Raw unprocessed log messages |
| `parsed_log_events` | Structured parsed log data with indexes |
| `analysis_results` | Computed analysis summaries |
| `alert_rules` | Configurable alert conditions |
| `alert_events` | Triggered alert instances |

---

## Project Structure

```
LogSphere/
├── src/main/java/com/ankur/loganalyzer/
│   ├── config/          # Spring configuration (JPA, Redis, Async, CORS)
│   ├── controller/      # REST API controllers
│   ├── service/         # Business logic (ingestion, processing, analysis, alerts)
│   ├── parser/          # Strategy-based log parsing (JSON, Regex, StackTrace)
│   ├── analyzer/        # Analysis engines
│   ├── client/          # External API clients (Loki)
│   ├── repository/      # JPA repositories
│   ├── model/           # JPA entities
│   ├── dto/             # Request/Response DTOs
│   ├── scheduler/       # Scheduled background jobs
│   └── exception/       # Custom exceptions and global error handling
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   └── application-prod.yml
├── compose.yaml
├── pom.xml
└── README.md
```

---

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven
- Docker Desktop (for PostgreSQL and Redis)

### Clone the repository

```bash
git clone https://github.com/ankur-roy-byte/LogSphere.git
cd LogSphere
```

### Start infrastructure

```bash
docker compose up -d
```

This starts PostgreSQL 17 and Redis 7 locally.

### Run the application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Or on Windows:

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

The application starts on `http://localhost:8080`.

### Test the upload endpoint

```bash
curl -X POST http://localhost:8080/api/logs/upload \
  -H "Content-Type: application/json" \
  -d '{"content": "2024-01-15 10:30:45.123 ERROR [my-service] [main] c.e.MyClass - NullPointerException: Something went wrong\n2024-01-15 10:30:46.456 INFO [my-service] [main] c.e.MyClass - Request completed successfully"}'
```

### Get analysis summary

```bash
curl http://localhost:8080/api/analysis/summary
```

---

## Planned Enhancements

- [ ] Kafka-based log streaming ingestion
- [ ] OpenSearch integration for full-text search
- [ ] Intelligent pattern clustering
- [ ] Real-time dashboard support
- [ ] Alert notifications (Email/Slack)
- [ ] Role-based access control with Spring Security
- [ ] UI dashboard with React or Angular
- [ ] AI-assisted anomaly explanation

---

## Current Status

**Project status:** In active development

The project foundation, core entities, parsing pipeline, concurrent processing engine, analysis services, alert system, and REST APIs are implemented. Current development focus:

- [x] Spring Boot project setup with Maven
- [x] PostgreSQL and Redis integration
- [x] JPA entities with auditing
- [x] Strategy-based log parser (JSON, Regex, StackTrace)
- [x] Concurrent batch processing with thread pools
- [x] Log ingestion from file upload
- [x] Grafana Loki API client
- [x] Log search with filters
- [x] Analysis summary endpoint
- [x] Spike detection
- [x] Alert rules and evaluation
- [x] Scheduled background jobs
- [ ] Kafka ingestion
- [ ] OpenSearch integration
- [ ] UI dashboard

---

## Author

**Ankur Roy**
GitHub: [ankur-roy-byte](https://github.com/ankur-roy-byte)

