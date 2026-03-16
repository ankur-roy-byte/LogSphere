# LogSphere
AI-powered log analyzer built with Java Spring Boot for concurrent log ingestion, parsing, search, and anomaly detection.

**LogSphere** is a professional-grade **AI-powered log analysis platform** built with **Java Spring Boot** for high-performance log ingestion, parsing, search, analytics, and anomaly detection.

It is designed to process logs from multiple sources concurrently using **multithreading and concurrency**, store structured results efficiently, and provide actionable operational insights through scalable REST APIs.

---

## Overview

Modern applications generate a huge volume of logs across services, environments, and infrastructure. Manually reading and analyzing those logs is slow, error-prone, and inefficient.

**LogSphere** solves this by providing a centralized backend system that can:

- ingest logs from external sources and APIs
- parse raw logs into structured data
- process multiple logs concurrently
- detect errors, repeated failures, and suspicious patterns
- generate summaries and insights
- support future alerting and anomaly detection workflows

This project is built as a scalable backend foundation for a production-ready log intelligence platform.

---

## Key Features

- **Concurrent log ingestion**
  - Handles multiple log streams and batches using multithreading

- **Structured log parsing**
  - Supports parsing raw, JSON, and application-style logs into normalized objects

- **REST API driven architecture**
  - Exposes APIs for ingestion, analysis, search, and monitoring

- **Open-source log source integration**
  - Planned integration with open-source log providers such as **Grafana Loki**

- **High-performance backend**
  - Uses **Spring Boot**, **Executor-based concurrency**, **Redis**, and **PostgreSQL**

- **Search and analysis**
  - Filter logs by severity, source, timestamp, trace ID, and service

- **Future-ready analytics**
  - Supports extensibility for anomaly detection, alerting, and intelligent log insights

---

## Tech Stack

### Backend
- **Java 21**
- **Spring Boot**
- **Spring Web**
- **Spring Data JPA**
- **Spring Validation**
- **Spring Boot Actuator**

### Database & Caching
- **PostgreSQL** – persistent storage for parsed logs and analysis results
- **Redis** – caching, job coordination, counters, and performance optimization

### Development Tools
- **Maven**
- **Lombok**
- **Docker Compose**
- **Spring Boot DevTools**

---

## Architecture

LogSphere follows a modular backend architecture:

```text
Log Source / API / File Upload
            |
            v
    Log Ingestion Layer
            |
            v
      Parsing Pipeline
            |
            v
  Concurrent Processing Engine
            |
            v
  Analysis + Aggregation Layer
            |
            v
 PostgreSQL / Redis Storage
            |
            v
        REST API Layer
