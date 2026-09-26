# Módulo 1: Fundamentos, Infraestructura y Health Check (Sprint 1 — US-101)

Este documento detalla a nivel técnico exhaustivo el **Paso 1** del proyecto **Integration Hub**, cubriendo la preparación del entorno, la infraestructura de persistencia con Docker y PostgreSQL, y la creación del backend en Spring Boot 3.3 con Java 21.

---

## 1. Alcance Técnico del Paso 1
- **Objetivo:** Inicializar la base del monolito modular y verificar la conectividad de extremo a extremo (Backend ➔ Base de Datos) mediante un endpoint de salud funcional.
- **Historia de Usuario Asociada:** `US-101: Scaffolding Backend y Base de Datos (3 SP)`.
- **Resultado Esperado (DoD):**
  - Contenedor PostgreSQL 16 corriendo en el puerto `5432`.
  - Proyecto Spring Boot 3.3.x estructurado y compilable con Maven y Java 21.
  - Endpoint `GET /api/v1/health` retornando `200 OK` con verificación de base de datos activa.
  - Documentación Swagger / OpenAPI disponible en `/swagger-ui.html`.

---

## 2. Paso a Paso Técnico

### Paso 1.1: Infraestructura con Docker Compose
Se define un archivo `docker-compose.yml` en la raíz del repositorio para garantizar paridad entre entornos de desarrollo y producción sin requerir instalaciones manuales complejas de bases de datos.

```yaml
version: '3.8'

services:
  integration-hub-db:
    image: postgres:16-alpine
    container_name: integration-hub-postgres
    restart: always
    environment:
      POSTGRES_DB: integration_hub_db
      POSTGRES_USER: hub_user
      POSTGRES_PASSWORD: hub_password_2026
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U hub_user -d integration_hub_db"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
    driver: local
```

---

### Paso 1.2: Estructura del Backend (Spring Boot 3.3.x + Java 21)
El código se alojará en el directorio `backend/` con el descriptor Maven `pom.xml`.

#### Dependencias Clave:
1. `spring-boot-starter-web`: Soporte para endpoints REST MVC.
2. `spring-boot-starter-data-jpa`: Capa de persistencia con Hibernate 6.
3. `postgresql`: Driver JDBC para PostgreSQL 16.
4. `spring-boot-starter-validation`: Jakarta Bean Validation (Hibernate Validator).
5. `spring-boot-starter-actuator`: Monitoreo y métricas de salud del sistema.
6. `springdoc-openapi-starter-webmvc-ui` (v2.6.0): Swagger UI interactivo.
7. `lombok`: Generación limpia de boilerplate (constructores, getters/setters, builders).

---

### Paso 1.3: Configuración del Sistema (`application.yml`)
Configuración en `backend/src/main/resources/application.yml`:

```yaml
server:
  port: 8080
  servlet:
    context-path: /

spring:
  application:
    name: integration-hub
  threads:
    virtual:
      enabled: true # Habilita Project Loom / Virtual Threads de Java 21

  datasource:
    url: jdbc:postgresql://localhost:5432/integration_hub_db
    username: hub_user
    password: hub_password_2026
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      idle-timeout: 30000
      connection-timeout: 20000

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method
```

---

### Paso 1.4: Clase Principal y Arquitectura de Paquetes
```
backend/src/main/java/com/integrationhub/
├── IntegrationHubApplication.java
└── shared/
    ├── domain/
    │   └── ApiResponse.java
    └── infrastructure/
        └── HealthController.java
```

#### Código de `HealthController.java`:
```java
package com.integrationhub.shared.infrastructure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Salud del Sistema", description = "Endpoints de diagnóstico de infraestructura")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    @Operation(summary = "Verificar estado del servicio y conectividad con la BD")
    public ResponseEntity<Map<String, Object>> getHealth() {
        boolean dbConnected = false;
        String dbMessage = "OK";

        try (Connection conn = dataSource.getConnection()) {
            dbConnected = !conn.isClosed();
        } catch (Exception e) {
            dbMessage = e.getMessage();
        }

        Map<String, Object> response = Map.of(
            "status", dbConnected ? "UP" : "DEGRADED",
            "timestamp", Instant.now().toString(),
            "service", "integration-hub-backend",
            "version", "1.0.0-MVP",
            "database", Map.of(
                "connected", dbConnected,
                "detail", dbMessage
            )
        );

        return dbConnected ? ResponseEntity.ok(response) : ResponseEntity.status(503).body(response);
    }
}
```

---

## 3. Criterios de Aceptación (Gherkin)

```gherkin
Feature: Diagnóstico de Salud y Scaffolding Inicial

  Scenario: Verificación de estado saludable con base de datos activa
    Given que el contenedor de PostgreSQL está en ejecución en el puerto 5432
    And la aplicación Spring Boot está iniciada
    When el cliente realiza una petición HTTP GET a "/api/v1/health"
    Then el código de respuesta debe ser 200 OK
    And el campo "status" debe ser "UP"
    And el campo "database.connected" debe ser true
```

---

## 4. Definition of Done (DoD) para este Paso
- [x] Documento técnico creado en `modulos/modulo1.md`.
- [x] Archivo `docker-compose.yml` creado con configuración de PostgreSQL 16.
- [x] `pom.xml` estructurado y dependencias descargadas con Java 21 y Maven 3.9.9.
- [x] `HealthController` y `HealthControllerTest` implementados.
- [x] **Pruebas unitarias ejecutadas:** `HealthControllerTest` 2/2 exitosas (`BUILD SUCCESS`).
- [ ] Base de datos PostgreSQL 16 inicializada en puerto 5432 con base de datos `integration_hub_db`.
- [ ] Aplicación levantada y respondiendo en `http://localhost:8080/api/v1/health`.
