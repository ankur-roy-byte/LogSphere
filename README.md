<p align="center">
  <h1 align="center">LogSphere</h1>
  <p align="center">
    <strong>Enterprise-Grade Log Analysis Platform</strong>
  </p>
  <p align="center">
    High-performance log ingestion, intelligent parsing, real-time analytics, and anomaly detection
  </p>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=openjdk" alt="Java 17+"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.x-green?style=flat-square&logo=springboot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/PostgreSQL-17-blue?style=flat-square&logo=postgresql" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Redis-7-red?style=flat-square&logo=redis" alt="Redis"/>
  <img src="https://img.shields.io/badge/Kafka-Ready-black?style=flat-square&logo=apachekafka" alt="Kafka"/>
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=flat-square" alt="License"/>
</p>

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Data Models](#data-models)
- [Concurrency & Performance](#concurrency--performance)
- [Alerting System](#alerting-system)
- [Caching Strategy](#caching-strategy)
- [Testing](#testing)
- [Deployment](#deployment)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

**LogSphere** is a production-ready, enterprise-grade log analysis platform designed to handle high-volume log ingestion, intelligent parsing, and real-time analytics at scale.

In modern distributed systems, applications generate massive volumes of logs across microservices, containers, and infrastructure components. Traditional log management approaches struggle with:

- **Volume**: Millions of log entries per hour
- **Variety**: Different log formats (JSON, plain text, stack traces)
- **Velocity**: Need for real-time insights and alerting
- **Value extraction**: Finding actionable insights from noise

**LogSphere** addresses these challenges by providing:

| Capability | Description |
|------------|-------------|
| **Multi-Source Ingestion** | File uploads, Grafana Loki, Apache Kafka streaming |
| **Intelligent Parsing** | Auto-detection of JSON, regex patterns, and stack traces |
| **Concurrent Processing** | Multi-threaded pipeline processing 10,000+ logs/second |
| **Real-Time Analytics** | Summary statistics, aggregations, and trend analysis |
| **Anomaly Detection** | Z-score analysis, spike detection, pattern clustering |
| **Alerting Engine** | Rule-based alerts with multiple condition types |
| **Performance Caching** | Redis-backed caching for sub-millisecond response times |

---

## Key Features

### Log Ingestion
- **File Upload** — Direct log content upload via REST API
- **Grafana Loki Integration** — Query and ingest logs from Loki instances
- **Apache Kafka Streaming** — Real-time log consumption from Kafka topics
- **Webhook Support** — Extensible webhook ingestion (planned)

### Intelligent Parsing
- **Auto-Format Detection** — Automatically detects JSON, Spring Boot, and plain text formats
- **Stack Trace Extraction** — Parses Java/Python exceptions with full stack trace preservation
- **Metadata Extraction** — Extracts service name, trace ID, host, log level, and custom fields
- **Multiline Support** — Handles multiline stack traces and continued log entries

### Analytics & Insights
- **Summary Statistics** — Total counts, error rates, warning rates by time window
- **Aggregations** — Group by service, level, host, exception type with time bucketing
- **Pattern Detection** — Clusters similar log messages to identify repeated issues
- **Anomaly Detection** — Statistical Z-score analysis to detect unusual patterns
- **Spike Detection** — Hour-over-hour comparison to identify sudden increases

### Alerting System
- **Configurable Rules** — Create custom alert conditions
- **Multiple Condition Types** — Error thresholds, spikes, pattern matching, repeated messages
- **Automatic Evaluation** — Scheduled background evaluation of all rules
- **Alert Events** — Persistent alert history with resolution tracking

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              LOG SOURCES                                     │
├─────────────────┬─────────────────┬─────────────────┬───────────────────────┤
│   File Upload   │   Grafana Loki  │   Apache Kafka  │      Webhooks         │
│   (REST API)    │   (HTTP Client) │    (Consumer)   │      (Planned)        │
└────────┬────────┴────────┬────────┴────────┬────────┴───────────┬───────────┘
         │                 │                 │                     │
         └─────────────────┴────────┬────────┴─────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         INGESTION LAYER                                      │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  ThreadPoolTaskExecutor: ingestion-* (4 core / 8 max / 500 queue)   │    │
│  │  • Batch partitioning (500 logs/batch)                               │    │
│  │  • Multiline log aggregation                                         │    │
│  │  • Raw log persistence                                               │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────┬───────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          PARSING PIPELINE                                    │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  ThreadPoolTaskExecutor: parsing-* (8 core / 16 max / 1000 queue)   │    │
│  │                                                                      │    │
│  │  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐           │    │
│  │  │ JsonLogParser│───▶│RegexLogParser│───▶│StackTrace   │           │    │
│  │  │   (Order 1)  │    │   (Order 2)  │    │Parser (Ord 3)│           │    │
│  │  └──────────────┘    └──────────────┘    └──────────────┘           │    │
│  │         │                   │                   │                    │    │
│  │         └───────────────────┴───────────────────┘                    │    │
│  │                             │                                        │    │
│  │                    ┌────────▼────────┐                               │    │
│  │                    │  ParserFactory  │                               │    │
│  │                    │ (Strategy Pattern)                              │    │
│  │                    └─────────────────┘                               │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────┬───────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        ANALYSIS ENGINE                                       │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  ThreadPoolTaskExecutor: analysis-* (8 core / 16 max / 500 queue)   │    │
│  │                                                                      │    │
│  │  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐         │    │
│  │  │ Aggregation    │  │   Anomaly      │  │    Pattern     │         │    │
│  │  │   Service      │  │   Detector     │  │   Detector     │         │    │
│  │  ├────────────────┤  ├────────────────┤  ├────────────────┤         │    │
│  │  │• Time buckets  │  │• Z-score       │  │• Message       │         │    │
│  │  │• By service    │  │• Spike detect  │  │  clustering    │         │    │
│  │  │• By level      │  │• Service-level │  │• Stack trace   │         │    │
│  │  │• Statistics    │  │  anomalies     │  │  signatures    │         │    │
│  │  └────────────────┘  └────────────────┘  └────────────────┘         │    │
│  │                                                                      │    │
│  │  ┌────────────────┐  ┌────────────────┐                             │    │
│  │  │    Error       │  │    Alert       │                             │    │
│  │  │  Classifier    │  │   Service      │                             │    │
│  │  ├────────────────┤  ├────────────────┤                             │    │
│  │  │• Severity      │  │• Rule eval     │                             │    │
│  │  │• Categories    │  │• Event trigger │                             │    │
│  │  └────────────────┘  └────────────────┘                             │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────┬───────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        PERSISTENCE LAYER                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  ThreadPoolTaskExecutor: persistence-* (2 core / 4 max / 200 queue) │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
│  ┌─────────────────────────┐          ┌─────────────────────────┐          │
│  │      PostgreSQL 17      │          │        Redis 7          │          │
│  ├─────────────────────────┤          ├─────────────────────────┤          │
│  │ • raw_log_events        │          │ • Analysis cache        │          │
│  │ • parsed_log_events     │          │ • Summary cache         │          │
│  │ • analysis_results      │          │ • Aggregation cache     │          │
│  │ • alert_rules           │          │ • TTL: 5 minutes        │          │
│  │ • alert_events          │          │                         │          │
│  │ • log_sources           │          │                         │          │
│  └─────────────────────────┘          └─────────────────────────┘          │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           REST API LAYER                                     │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │     Log     │ │  Analysis   │ │    Alert    │ │   Actuator  │           │
│  │ Controller  │ │ Controller  │ │ Controller  │ │  Endpoints  │           │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## High-Level Design (HLD)

### System Overview

LogSphere follows a **layered microservices pattern** with clear separation of concerns:

```
┌──────────────────────────────────────────────────────────────┐
│                    CLIENT LAYER                              │
│         REST API Clients, Dashboards, Mobile Apps            │
└──────────────────┬───────────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────┐
│                   API LAYER                                  │
│  LogController | AnalysisController | AlertController       │
│         (OpenAPI 3.0 / Swagger UI)                          │
└──────────────────┬───────────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────┐
│                   SERVICE LAYER                              │
│  LogIngestionService    LogProcessingService                │
│  LogAnalysisService     AlertService                         │
│  LogSearchService       LokiClientService                    │
└──────────────────┬───────────────────────────────────────────┘
                   │
         ┌─────────┴─────────┐
         │                   │
┌────────▼──────┐   ┌────────▼──────┐
│ ANALYZER LAYER│   │PARSER LAYER    │
│               │   │                │
│ • Aggregation │   │• JsonLogParser │
│ • Anomaly     │   │• RegexLogParser│
│ • Pattern     │   │• StackTrace    │
│ • Error Class │   │  Parser        │
│ • AlertEngine │   │• ParserFactory │
└────────┬──────┘   └────────┬──────┘
         │                   │
         └─────────┬─────────┘
                   │
┌──────────────────▼──────────────────────────────────────────┐
│              REPOSITORY LAYER (JPA)                          │
│  RawLogEventRepository    ParsedLogEventRepository           │
│  AlertRuleRepository      AlertEventRepository               │
│  AnalysisResultRepository LogSourceRepository                │
└──────────────────┬───────────────────────────────────────────┘
                   │
         ┌─────────┴──────────┐
         │                    │
    ┌────▼─────┐         ┌────▼──────┐
    │PostgreSQL│         │  Redis    │
    │  (RW)    │         │ (Cache)   │
    └──────────┘         └───────────┘
```

### Component Interactions

1. **Request Flow**: Client → API Controller → Service → Repository → Database
2. **Async Processing**: CompletableFuture for non-blocking batch operations
3. **Caching Strategy**: Redis with 5-minute TTL for analysis results
4. **Error Handling**: Global exception handler with standardized error responses

### Key Design Principles

| Principle | Implementation |
|-----------|----------------|
| **Separation of Concerns** | Distinct layers (controller → service → repository) |
| **Concurrency** | 4 bounded thread pools prevent resource exhaustion |
| **Scalability** | Batch processing in configurable batch sizes |
| **Resilience** | Async processing with error handling and fallbacks |
| **Observability** | Structured logging, metrics, health checks |

---

## Low-Level Design (LLD)

### Package Structure

```
com.ankur.loganalyzer
├── config/          # Configuration beans
│   ├── AsyncConfig.java       # Thread pools (4 executors)
│   ├── RedisConfig.java       # Redis template setup
│   ├── JpaConfig.java         # JPA/Hibernate config
│   ├── WebConfig.java         # Web MVC config
│   ├── OpenApiConfig.java     # Swagger 3.0 setup
│   ├── LoggingConfig.java     # Logging profiles
│   └── KafkaConfig.java       # Kafka (disabled)
│
├── controller/      # REST endpoints
│   ├── LogController.java     # POST /api/logs/upload, GET /api/logs/search
│   ├── AnalysisController.java# GET /api/analysis/*
│   ├── AlertController.java   # POST /api/alerts/rules
│   ├── LokiFetchController.java # POST /api/logs/fetch/loki
│   └── KafkaFetchController.java # POST /api/logs/fetch/kafka
│
├── service/         # Business logic
│   ├── LogIngestionService.java    # Batch ingestion, multiline handling
│   ├── LogProcessingService.java   # Concurrent parsing pipeline
│   ├── LogAnalysisService.java     # Summary, aggregation, caching
│   ├── LogSearchService.java       # Dynamic search with pagination
│   └── AlertService.java           # Rule evaluation, event generation
│
├── parser/          # Log parsing (Strategy pattern)
│   ├── LogParser.java         # Interface
│   ├── JsonLogParser.java     # @Component, Order=1
│   ├── RegexLogParser.java    # @Component, Order=2
│   ├── StackTraceParser.java  # @Component, Order=3
│   └── ParserFactory.java     # Strategy selection
│
├── analyzer/        # Advanced analytics
│   ├── AggregationService.java # Group-by, time buckets
│   ├── AnomalyDetector.java    # Z-score, spike detection
│   ├── PatternDetector.java    # Message clustering
│   ├── ErrorClassifier.java    # Severity mapping
│   └── AlertRuleEvaluator.java # Condition evaluation
│
├── repository/      # Data access (JPA)
│   ├── RawLogEventRepository.java      # Native @Query for batching
│   ├── ParsedLogEventRepository.java   # Custom with aggregations
│   ├── AlertRuleRepository.java        # CRUD + search
│   ├── AlertEventRepository.java       # Event persistence
│   ├── AnalysisResultRepository.java   # Result caching
│   └── LogSourceRepository.java        # Source management
│
├── entity/          # JPA entities
│   ├── BaseEntity.java         # @MappedSuperclass, id + timestamps
│   ├── RawLogEvent.java        # @Entity, @Table with indexes
│   ├── ParsedLogEvent.java     # @Entity with JSON metadata
│   ├── AlertRule.java          # @Entity with condition enum
│   ├── AlertEvent.java         # @Entity for alerts
│   ├── AnalysisResult.java     # @Entity for cached results
│   └── LogSource.java          # @Entity for source metadata
│
├── dto/             # Request/response objects
│   ├── LogUploadRequest.java       # Input: content, sourceName
│   ├── LogUploadResponse.java      # Output: counts, message
│   ├── ParsedLogResponse.java      # Response: log fields
│   ├── LogSearchRequest.java       # Search criteria
│   ├── AnalysisSummaryResponse.java# Summary stats
│   ├── AggregationResponse.java    # Aggregated results
│   ├── AnomalyDetectionResponse.java # Anomaly details
│   ├── AlertRuleRequest.java       # Rule creation
│   ├── ApiResponse<T>.java         # Generic wrapper
│   └── ErrorResponse.java          # Error details
│
├── exception/       # Error handling
│   ├── GlobalExceptionHandler.java # @RestControllerAdvice
│   ├── ResourceNotFoundException.java # Custom exception
│   └── LogAnalysisException.java   # Analysis error
│
├── client/          # External integrations
│   ├── LokiClientService.java    # Grafana Loki HTTP client
│   └── KafkaConsumerService.java # Kafka consumer (disabled)
│
├── scheduler/       # Background tasks
│   └── LogAnalysisScheduler.java # @Scheduled analysis runs
│
├── util/            # Utilities
│   ├── PaginationUtils.java     # Safe pagination params
│   └── StringUtils.java         # Exception/trace ID extraction
│
└── Application.java # @SpringBootApplication entry point
```

### Key Classes & Responsibilities

#### **Entities** (JPA)

| Entity | Key Fields | Indexes | Purpose |
|--------|-----------|---------|---------|
| `RawLogEvent` | id, source, rawMessage, timestamp, traceId, ingestionTime | timestamp, source_id | Store unprocessed logs |
| `ParsedLogEvent` | id, rawEvent, serviceName, level, message, exceptionType, stackTrace, metadata | timestamp, level, serviceName, traceId | Store parsed structured data |
| `AlertRule` | id, name, conditionType, threshold, serviceName, enabled | serviceName | Define alert conditions |
| `AlertEvent` | id, rule, message, triggeredAt, resolved, resolvedAt | rule_id, triggeredAt | Persistent alert history |
| `AnalysisResult` | id, analysisType, resultKey, resultValue, windowStart, windowEnd, generatedAt | windowStart, analysisType | Cache computed results |

#### **Services** (Service Layer)

```
LogIngestionService
  ├─ ingestFromUpload(content, sourceName) → IngestionResult
  ├─ ingestRawLines(lines, source) → IngestionResult
  ├─ processBatch(lines, source) [async]
  └─ partition(list, batchSize) [utility]

LogProcessingService
  ├─ processUnprocessedLogs() [async]
  ├─ processBatchAsync(logs, batchSize)
  └─ updateProcessedStatus(ids)

LogAnalysisService
  ├─ generateSummary(start, end) → AnalysisSummaryResponse
  ├─ detectAnomalies(start, end, windowSize) → AnomalyDetectionResponse
  ├─ detectSpikes() → SpikeDetectionResponse
  ├─ analyzePatterns(start, end, limit) → PatternAnalysisResponse
  ├─ getAggregations(start, end, bucketSize) → AggregationResponse
  └─ [caching logic with Redis TTL]

AlertService
  ├─ createRule(request) → AlertRule
  ├─ evaluateAllRules() [scheduled]
  ├─ triggerAlert(rule, context)
  └─ updateAlertStatus(id, resolved)

LogSearchService
  ├─ searchLogs(serviceName, level, traceId, ...) → Page<ParsedLogResponse>
  ├─ getById(id) → ParsedLogResponse
  └─ [Dynamic JPA Specifications for filters]
```

#### **Parser Framework** (Strategy Pattern)

```
LogParser (Interface)
  ├─ supports(rawLog): boolean
  └─ parse(rawLog): ParsedLogEventBuilder

JsonLogParser (@Order=1)
  ├─ Tries JSONObject parsing
  ├─ Extracts: serviceName, level, message, timestamp
  └─ Returns ParsedLogEventBuilder

RegexLogParser (@Order=2)
  ├─ Matches patterns: "[LEVEL]", "serviceName", "timestamp"
  ├─ Extracts fields via regex groups
  └─ Returns ParsedLogEventBuilder

StackTraceParser (@Order=3)
  ├─ Handles multiline exceptions
  ├─ Extracts: exceptionType, full stackTrace
  └─ Returns ParsedLogEventBuilder

ParserFactory
  ├─ Maintains List<LogParser> (auto-wired)
  ├─ parse(rawLog)
  └─ Tries each parser in order, fallback to INFO plain text
```

#### **Analyzers** (Advanced Analytics)

```
AggregationService
  ├─ aggregateByService(events): Map<String, Long>
  ├─ aggregateByLevel(events): Map<LogLevel, Long>
  ├─ aggregateByExceptionType(events): Map<String, Long>
  ├─ aggregateByHost(events): Map<String, Long>
  └─ aggregateByTimeBucket(events, minutesBucket): Map<Instant, Long>

AnomalyDetector
  ├─ detectAnomaliesWithZScore(timeSeries): List<Anomaly>
  ├─ detectSpikes(timeSeries, windowSize): List<Anomaly>
  ├─ detectServiceAnomalies(current, baseline): List<ServiceAnomaly>
  └─ [Z-score threshold = ±2.576 (99% confidence)]

PatternDetector
  ├─ findTopPatterns(events, limit): List<PatternOccurrence>
  ├─ groupByPattern(events): Map<String, List<String>>
  └─ [Message similarity clustering]

ErrorClassifier
  ├─ classifyBySeverity(level): SeverityLevel
  ├─ extractCategory(exceptionType): ErrorCategory
  └─ [Enum-based classification]
```

### Thread Pool Configuration

```
ingestion-*
├─ Core size: 4
├─ Max size: 8
├─ Queue: 500 (BlockingQueue)
└─ Usage: Batch ingestion, multiline aggregation

parsing-*
├─ Core size: 8
├─ Max size: 16
├─ Queue: 1000
└─ Usage: Concurrent log parsing

analysis-*
├─ Core size: 8
├─ Max size: 16
├─ Queue: 500
└─ Usage: Analytics computation, aggregation

persistence-*
├─ Core size: 2
├─ Max size: 4
├─ Queue: 200
└─ Usage: Database batch writes
```

### Database Schema Overview

```sql
-- Raw logs (unprocessed stream)
CREATE TABLE raw_log_events (
  id BIGSERIAL PRIMARY KEY,
  source_id BIGINT REFERENCES log_sources(id),
  raw_message TEXT NOT NULL,
  timestamp TIMESTAMP NOT NULL,
  trace_id VARCHAR,
  host VARCHAR,
  ingestion_time TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_raw_log_timestamp ON raw_log_events(timestamp);
CREATE INDEX idx_raw_log_source ON raw_log_events(source_id);

-- Parsed logs (structured, indexed for search)
CREATE TABLE parsed_log_events (
  id BIGSERIAL PRIMARY KEY,
  raw_event_id BIGINT UNIQUE REFERENCES raw_log_events(id),
  service_name VARCHAR,
  level VARCHAR NOT NULL,  -- TRACE, DEBUG, INFO, WARN, ERROR, FATAL
  message TEXT NOT NULL,
  exception_type VARCHAR,
  stack_trace TEXT,
  timestamp TIMESTAMP NOT NULL,
  trace_id VARCHAR,
  host VARCHAR,
  metadata JSONB,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_parsed_log_timestamp ON parsed_log_events(timestamp);
CREATE INDEX idx_parsed_log_service ON parsed_log_events(service_name);
CREATE INDEX idx_parsed_log_level ON parsed_log_events(level);
CREATE INDEX idx_parsed_log_trace_id ON parsed_log_events(trace_id);

-- Alert rules (condition definitions)
CREATE TABLE alert_rules (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR NOT NULL UNIQUE,
  condition_type VARCHAR NOT NULL,
  threshold INT,
  service_name VARCHAR,
  enabled BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP
);

-- Alert events (triggered alerts)
CREATE TABLE alert_events (
  id BIGSERIAL PRIMARY KEY,
  rule_id BIGINT REFERENCES alert_rules(id),
  message TEXT NOT NULL,
  triggered_at TIMESTAMP NOT NULL,
  resolved BOOLEAN DEFAULT false,
  resolved_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_alert_rule ON alert_events(rule_id);
CREATE INDEX idx_alert_triggered ON alert_events(triggered_at);
```

### Design Patterns Used

| Pattern | Location | Purpose |
|---------|----------|---------|
| **Strategy Pattern** | `ParserFactory` + `LogParser` | Multiple parsing strategies with runtime selection |
| **Builder Pattern** | Entity classes | Complex object construction (Lombok @Builder) |
| **Dependency Injection** | Across classes | Constructor-based injection for testability |
| **Repository Pattern** | `*Repository` classes | Data access abstraction |
| **Service Locator** | `ParserFactory` | Discovers and selects appropriate parser |
| **Async/Callback** | `CompletableFuture` | Non-blocking concurrent processing |
| **Template Method** | `LogAnalysisService` | Caching logic repeated pattern |

---

### Component Responsibilities

| Component | Responsibility |
|-----------|----------------|
| **LogController** | Log upload, search, and retrieval endpoints |
| **AnalysisController** | Analytics, aggregations, patterns, anomalies |
| **AlertController** | Alert rule management and event retrieval |
| **LokiFetchController** | Grafana Loki integration |
| **KafkaFetchController** | Apache Kafka integration |
| **LogIngestionService** | Batch ingestion and raw log storage |
| **LogProcessingService** | Concurrent parsing with CompletableFuture |
| **LogAnalysisService** | Summary, aggregation, and analysis generation |
| **AlertService** | Rule evaluation and alert triggering |
| **LogSearchService** | JPA Specification-based dynamic search |
| **LogAnalysisScheduler** | Background scheduled tasks |

---

## Technology Stack

### Core Framework
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17+ | Programming language |
| Spring Boot | 4.x | Application framework |
| Spring Web | - | REST API development |
| Spring Data JPA | - | ORM and database access |
| Spring Validation | - | Request validation |
| Spring Kafka | - | Kafka integration |
| Jackson | - | JSON serialization |

### Data Storage
| Technology | Version | Purpose |
|------------|---------|---------|
| PostgreSQL | 17 | Primary persistent storage |
| Redis | 7 | Caching and performance optimization |
| Apache Kafka | 3.x | Event streaming (optional) |

### Infrastructure
| Technology | Purpose |
|------------|---------|
| Docker Compose | Local development infrastructure |
| Apache Zookeeper | Kafka coordination |
| Spring Boot Actuator | Health checks and metrics |

### Development
| Technology | Purpose |
|------------|---------|
| Maven | Build and dependency management |
| Lombok | Boilerplate code reduction |
| JUnit 5 | Unit testing framework |
| Spring Boot DevTools | Hot reload during development |

---

## Getting Started

### Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| Java JDK | 17+ | OpenJDK or Oracle JDK |
| Maven | 3.8+ | For building the project |
| Docker | 20+ | For running infrastructure |
| Docker Compose | 2.0+ | For orchestrating containers |

### Quick Start

#### 1. Clone the Repository

```bash
git clone https://github.com/ankur-roy-byte/LogSphere.git
cd LogSphere
```

#### 2. Start Infrastructure

```bash
docker compose up -d
```

This starts:
- PostgreSQL 17 on port `5432`
- Redis 7 on port `6379`
- Zookeeper on port `2181`
- Kafka on port `9092`

#### 3. Verify Infrastructure

```bash
docker compose ps
```

All containers should show `healthy` status.

#### 4. Run the Application

**Linux/macOS:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Windows:**
```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

#### 5. Verify Application

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### First Log Ingestion

```bash
curl -X POST http://localhost:8080/api/logs/upload \
  -H "Content-Type: application/json" \
  -d '{
    "content": "2024-01-15 10:30:45.123 ERROR [payment-service] [main] c.e.PaymentController - NullPointerException: Payment failed\n2024-01-15 10:30:46.456 INFO [payment-service] [main] c.e.PaymentController - Retry successful",
    "sourceName": "payment-service"
  }'
```

Expected response:
```json
{
  "totalLines": 2,
  "parsedSuccessfully": 2,
  "parseFailures": 0,
  "message": "Log ingestion complete"
}
```

---

## Configuration

### Application Profiles

| Profile | Purpose | Configuration File |
|---------|---------|-------------------|
| `default` | Base configuration | `application.yml` |
| `dev` | Local development | `application-dev.yml` |
| `prod` | Production deployment | `application-prod.yml` |

### Key Configuration Properties

#### Database Configuration

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/loganalyzer
    username: loganalyzer
    password: loganalyzer
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  jpa:
    hibernate:
      ddl-auto: update  # Use 'validate' in production
    show-sql: false
```

#### Redis Configuration

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
```

#### Kafka Configuration

```yaml
kafka:
  enabled: true  # Set to false to disable Kafka
  bootstrap-servers: localhost:9092
  consumer:
    group-id: logsphere-consumer-group
    auto-offset-reset: earliest
    concurrency: 3
  topic:
    logs: logs
```

#### Scheduler Configuration

```yaml
scheduler:
  analysis:
    rate: 300000   # 5 minutes
  alert:
    rate: 60000    # 1 minute
  spike:
    rate: 600000   # 10 minutes
```

#### Loki Configuration

```yaml
loki:
  base-url: http://localhost:3100
```

### Environment Variables (Production)

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection URL | - |
| `DATABASE_USERNAME` | Database username | - |
| `DATABASE_PASSWORD` | Database password | - |
| `REDIS_HOST` | Redis host | localhost |
| `REDIS_PORT` | Redis port | 6379 |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | localhost:9092 |
| `LOKI_BASE_URL` | Grafana Loki URL | http://localhost:3100 |

---

## API Reference

### Base URL

```
http://localhost:8080/api
```

### Authentication

> **Note:** Authentication is not yet implemented. All endpoints are currently public.

---

### Log Ingestion APIs

#### Upload Logs

```http
POST /api/logs/upload
Content-Type: application/json
```

**Request Body:**
```json
{
  "content": "2024-01-15 10:30:45 ERROR [service] Message here\n...",
  "sourceName": "my-service",
  "format": "auto"
}
```

**Response:**
```json
{
  "totalLines": 100,
  "parsedSuccessfully": 98,
  "parseFailures": 2,
  "message": "Log ingestion complete"
}
```

#### Fetch from Loki

```http
POST /api/logs/fetch/loki
Content-Type: application/json
```

**Request Body:**
```json
{
  "query": "{job=\"my-app\"}",
  "startNs": 1705312245000000000,
  "endNs": 1705315845000000000,
  "limit": 1000
}
```

#### Fetch from Kafka

```http
POST /api/logs/fetch/kafka
Content-Type: application/json
```

**Request Body:**
```json
{
  "topic": "logs",
  "partition": 0,
  "offset": 100,
  "limit": 500
}
```

---

### Log Search APIs

#### Search Logs

```http
GET /api/logs/search
```

**Query Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `serviceName` | string | Filter by service name |
| `level` | string | Filter by log level (ERROR, WARN, INFO, DEBUG, TRACE) |
| `traceId` | string | Filter by trace/correlation ID |
| `keyword` | string | Search in message content |
| `host` | string | Filter by host |
| `startTime` | ISO-8601 | Start of time range |
| `endTime` | ISO-8601 | End of time range |
| `page` | int | Page number (default: 0) |
| `size` | int | Page size (default: 20) |

**Example:**
```bash
curl "http://localhost:8080/api/logs/search?serviceName=payment-service&level=ERROR&page=0&size=50"
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "serviceName": "payment-service",
      "level": "ERROR",
      "message": "Payment failed",
      "exceptionType": "NullPointerException",
      "timestamp": "2024-01-15T10:30:45Z",
      "traceId": "abc123"
    }
  ],
  "totalElements": 150,
  "totalPages": 3,
  "number": 0
}
```

#### Get Log by ID

```http
GET /api/logs/{id}
```

---

### Analysis APIs

#### Get Summary

```http
GET /api/analysis/summary
```

**Query Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `startTime` | ISO-8601 | Start of analysis window |
| `endTime` | ISO-8601 | End of analysis window |

**Response:**
```json
{
  "totalLogs": 10000,
  "totalErrors": 150,
  "totalWarnings": 500,
  "totalInfo": 9350,
  "errorsByService": {
    "payment-service": 75,
    "user-service": 50,
    "order-service": 25
  },
  "topExceptions": [
    {"exceptionType": "NullPointerException", "count": 45},
    {"exceptionType": "SQLException", "count": 30}
  ],
  "windowStart": "2024-01-15T09:00:00Z",
  "windowEnd": "2024-01-15T10:00:00Z",
  "generatedAt": "2024-01-15T10:00:05Z"
}
```

#### Get Aggregations

```http
GET /api/analysis/aggregations
```

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `startTime` | ISO-8601 | 1 hour ago | Start time |
| `endTime` | ISO-8601 | now | End time |
| `bucketSizeMinutes` | long | 5 | Time bucket size |

**Response:**
```json
{
  "byService": {"payment-service": 500, "user-service": 300},
  "byLevel": {"ERROR": 150, "WARN": 500, "INFO": 9350},
  "byExceptionType": {"NullPointerException": 45},
  "byHost": {"server-1": 5000, "server-2": 5000},
  "byTimeBucket": {
    "2024-01-15T09:00:00Z": 1500,
    "2024-01-15T09:05:00Z": 1600
  },
  "totalLogs": 10000
}
```

#### Detect Anomalies

```http
GET /api/analysis/anomalies
```

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `startTime` | ISO-8601 | 24 hours ago | Start time |
| `endTime` | ISO-8601 | now | End time |
| `windowSize` | int | 5 | Moving average window |

**Response:**
```json
{
  "timeSeriesAnomalies": [
    {
      "timestamp": "2024-01-15T09:30:00Z",
      "actualValue": 500,
      "expectedValue": 100,
      "zScore": 4.5,
      "type": "SPIKE"
    }
  ],
  "serviceAnomalies": [
    {
      "serviceName": "payment-service",
      "currentCount": 200,
      "baselineCount": 50,
      "changePercent": 300.0,
      "type": "SPIKE"
    }
  ],
  "statistics": {
    "min": 10,
    "max": 500,
    "mean": 100,
    "median": 85,
    "stdDev": 50
  }
}
```

#### Analyze Patterns

```http
GET /api/analysis/patterns
```

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `startTime` | ISO-8601 | 1 hour ago | Start time |
| `endTime` | ISO-8601 | now | End time |
| `limit` | int | 20 | Max patterns to return |

**Response:**
```json
{
  "topPatterns": [
    {
      "pattern": "Connection timeout for user <NUM>",
      "count": 150,
      "exampleMessage": "Connection timeout for user 12345"
    }
  ],
  "totalPatternsFound": 45
}
```

#### Detect Spikes

```http
GET /api/analysis/spikes
```

**Response:**
```json
{
  "spikes": [
    {
      "serviceName": "payment-service",
      "level": "ERROR",
      "currentCount": 150,
      "previousCount": 50,
      "changePercentage": 200.0,
      "windowStart": "2024-01-15T09:00:00Z",
      "windowEnd": "2024-01-15T10:00:00Z"
    }
  ],
  "analysisTime": "2024-01-15T10:00:00Z"
}
```

#### Get Errors by Level

```http
GET /api/analysis/errors/by-level
```

#### Get Errors by Service

```http
GET /api/analysis/errors/by-service
```

#### Get Top Exceptions

```http
GET /api/analysis/errors/top?limit=10
```

#### Get Repeated Messages

```http
GET /api/analysis/exceptions?minCount=5&limit=20
```

---

### Alert APIs

#### Create Alert Rule

```http
POST /api/alerts/rules
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "High Error Rate",
  "conditionType": "ERROR_COUNT_EXCEEDS",
  "threshold": 100,
  "serviceName": "payment-service",
  "description": "Alert when errors exceed 100/hour"
}
```

**Condition Types:**

| Type | Description |
|------|-------------|
| `ERROR_COUNT_EXCEEDS` | Triggers when error count exceeds threshold |
| `LOGS_PER_MINUTE_EXCEEDS` | Triggers when log rate exceeds threshold |
| `SPIKE_DETECTED` | Triggers on percentage increase above threshold |
| `EXCEPTION_TYPE_MATCH` | Triggers when specific exception type count exceeds threshold |
| `REPEATED_MESSAGE_THRESHOLD` | Triggers when same message repeats above threshold |

#### List Alert Rules

```http
GET /api/alerts/rules
```

#### Delete Alert Rule

```http
DELETE /api/alerts/rules/{id}
```

#### Get Alert Events

```http
GET /api/alerts?page=0&size=20
```

#### Trigger Manual Evaluation

```http
POST /api/alerts/test
```

---

### Monitoring APIs

#### Health Check

```http
GET /actuator/health
```

#### Application Info

```http
GET /actuator/info
```

#### Metrics

```http
GET /actuator/metrics
```

---

## Data Models

### Core Entities

#### ParsedLogEvent

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `serviceName` | String | Originating service |
| `level` | LogLevel | TRACE, DEBUG, INFO, WARN, ERROR, FATAL |
| `message` | String | Log message content |
| `exceptionType` | String | Exception class name (if present) |
| `stackTrace` | String | Full stack trace (if present) |
| `timestamp` | Instant | Log event timestamp |
| `traceId` | String | Distributed tracing ID |
| `host` | String | Originating host |
| `metadata` | Map | Additional extracted fields |

#### RawLogEvent

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `source` | LogSource | Reference to log source |
| `rawMessage` | String | Original log text |
| `timestamp` | Instant | Event timestamp |
| `ingestionTime` | Instant | When log was received |

#### AlertRule

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `name` | String | Rule name |
| `conditionType` | ConditionType | Type of condition |
| `threshold` | int | Trigger threshold |
| `serviceName` | String | Optional service filter |
| `enabled` | boolean | Whether rule is active |

#### AlertEvent

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `rule` | AlertRule | Associated rule |
| `message` | String | Alert details |
| `triggeredAt` | Instant | When alert triggered |
| `resolved` | boolean | Resolution status |
| `resolvedAt` | Instant | When resolved |

---

## Concurrency & Performance

### Thread Pool Configuration

| Pool | Core Threads | Max Threads | Queue Capacity | Purpose |
|------|--------------|-------------|----------------|---------|
| `ingestion-*` | 4 | 8 | 500 | Log ingestion |
| `parsing-*` | 8 | 16 | 1000 | Log parsing |
| `analysis-*` | 8 | 16 | 500 | Analysis tasks |
| `persistence-*` | 2 | 4 | 200 | Database writes |

### Performance Characteristics

| Metric | Value | Notes |
|--------|-------|-------|
| Ingestion throughput | 10,000+ logs/sec | Depends on log size |
| Parsing latency | < 1ms/log | Average for JSON logs |
| Search response time | < 100ms | With PostgreSQL indexes |
| Cached query response | < 10ms | Redis-cached results |
| Memory footprint | 512MB - 2GB | Depends on batch sizes |

### Optimization Techniques

- **Batch Processing**: Logs processed in batches of 500
- **CompletableFuture Pipeline**: Non-blocking async processing
- **Bounded Queues**: Backpressure handling prevents OOM
- **Connection Pooling**: HikariCP for database connections
- **Redis Caching**: 5-minute TTL for analysis results
- **Database Indexes**: Optimized for common query patterns

---

## Alerting System

### Alert Condition Types

| Condition | Description | Use Case |
|-----------|-------------|----------|
| `ERROR_COUNT_EXCEEDS` | Total errors > threshold | High error volume alerts |
| `LOGS_PER_MINUTE_EXCEEDS` | Log rate > threshold | Traffic spike detection |
| `SPIKE_DETECTED` | % increase > threshold | Sudden error increase |
| `EXCEPTION_TYPE_MATCH` | Specific exception count > threshold | Critical exception monitoring |
| `REPEATED_MESSAGE_THRESHOLD` | Same message > threshold times | Flood/loop detection |

### Example Alert Rules

**Critical Error Volume:**
```json
{
  "name": "Critical Error Volume",
  "conditionType": "ERROR_COUNT_EXCEEDS",
  "threshold": 500,
  "description": "Alerts when more than 500 errors in 1 hour"
}
```

**Payment Service Spike:**
```json
{
  "name": "Payment Spike Alert",
  "conditionType": "SPIKE_DETECTED",
  "threshold": 100,
  "serviceName": "payment-service",
  "description": "Alerts when payment errors increase by 100%"
}
```

**Database Exception Monitor:**
```json
{
  "name": "Database Exception Alert",
  "conditionType": "EXCEPTION_TYPE_MATCH",
  "threshold": 10,
  "description": "SQLException"
}
```

---

## Caching Strategy

### Cache Keys

| Key Pattern | TTL | Description |
|-------------|-----|-------------|
| `logsphere:analysis:summary:{start}:{end}` | 5 min | Analysis summaries |
| `logsphere:analysis:aggregations:{start}:{end}` | 5 min | Aggregation results |

### Cache Invalidation

- TTL-based expiration (5 minutes)
- No manual invalidation (eventual consistency)
- Cache misses trigger fresh computation

---

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ParserFactoryTest

# Run with coverage report
mvn test jacoco:report
```

### Test Coverage

| Component | Test Class | Test Count |
|-----------|------------|------------|
| Parser Framework | `ParserFactoryTest` | 12 |
| Pattern Detection | `PatternDetectorTest` | 10 |
| Anomaly Detection | `AnomalyDetectorTest` | 11 |

### Test Categories

- **Unit Tests**: Parser, analyzer, and service logic
- **Integration Tests**: Repository and controller tests (planned)
- **Load Tests**: Performance benchmarking (planned)

---

## Deployment

### Docker Deployment

```bash
# Build the application
mvn clean package -DskipTests

# Build Docker image
docker build -t logsphere:latest .

# Run with Docker Compose
docker compose -f compose-prod.yaml up -d
```

### Production Checklist

- [ ] Set `spring.jpa.hibernate.ddl-auto=validate`
- [ ] Configure external PostgreSQL
- [ ] Configure external Redis
- [ ] Set appropriate JVM heap size
- [ ] Enable HTTPS/TLS
- [ ] Configure authentication (planned)
- [ ] Set up monitoring and alerting
- [ ] Configure log rotation

### Recommended JVM Options

```bash
java -Xms512m -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -jar logsphere.jar
```

---

## Roadmap

### Completed Features

- [x] Multi-format log parsing (JSON, Regex, Stack Trace)
- [x] Concurrent batch processing pipeline
- [x] Grafana Loki integration
- [x] Apache Kafka integration
- [x] Advanced anomaly detection (Z-score)
- [x] Pattern detection and clustering
- [x] Full alerting system (5 condition types)
- [x] Redis caching for performance
- [x] Scheduled background analysis
- [x] Comprehensive unit tests

### Planned Features

- [ ] OpenSearch/Elasticsearch integration
- [ ] Real-time WebSocket dashboard
- [ ] Email/Slack alert notifications
- [ ] Role-based access control (RBAC)
- [ ] Multi-tenant support
- [ ] Log retention policies
- [ ] Custom parser plugins
- [ ] AI-assisted anomaly explanation
- [ ] Angular/React dashboard UI
- [ ] Kubernetes Helm charts

---

## Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

### Development Setup

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- Follow standard Java conventions
- Use meaningful variable and method names
- Add Javadoc for public APIs
- Include unit tests for new features

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Author

**Ankur Roy**

- GitHub: [@ankur-roy-byte](https://github.com/ankur-roy-byte)

---

<p align="center">
  Built with passion for observability and log analytics
</p>
