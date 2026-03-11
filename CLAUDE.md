# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MQBridge is a Spring Boot 4.0.3 web application (Java 21) intended to serve as a message queue bridge. Currently a scaffold with no business logic implemented yet.

- **Group/Artifact:** com.dira / mqbridge
- **Base package:** `com.dira.mqbridge`

## Build & Test Commands

```bash
# Build (skip tests)
./mvnw package -DskipTests

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=MqbridgeApplicationTests

# Run the application
./mvnw spring-boot:run
```

## Architecture

- **Entry point:** `MqbridgeApplication.java` — standard `@SpringBootApplication` bootstrap
- **Config:** `src/main/resources/application.properties` (minimal — only `spring.application.name`)
- **Dependencies:** Spring Web MVC, DevTools (runtime)
- **Test framework:** JUnit Jupiter via `spring-boot-starter-webmvc-test`
