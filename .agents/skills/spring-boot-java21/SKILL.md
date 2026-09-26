---
name: spring-boot-java21
description: Guía de estándares y mejores prácticas para el desarrollo con Java 21 (LTS) y Spring Boot 3.3.x en arquitectura monolito modular. Úsalo al implementar controladores, servicios, entidades JPA, seguridad y virtual threads.
---

# Skill: Spring Boot 3.3 & Java 21 Standards

Este skill define las pautas de arquitectura, patrones y buenas prácticas técnicas para el backend de **Integration Hub**.

## 1. Características Clave de Java 21
- **Virtual Threads:** Habilitados mediante `spring.threads.virtual.enabled: true` para operaciones de I/O bloqueantes hacia sistemas externos sin saturar el pool de threads del SO.
- **Records:** Utilizar `record` para DTOs inmutables, eventos y respuestas de API.
- **Pattern Matching:** Usar `switch` con type matching y exhaustiveness para manejo de tipos de autenticación y transformaciones.
- **Sequenced Collections:** Utilizar `getFirst()`, `getLast()` en listas y colecciones ordenadas.

## 2. Arquitectura Monolito Modular (Clean Architecture)
Cada paquete de dominio debe dividirse en 3 capas bien diferenciadas:
```
com.integrationhub.<modulo>/
├── domain/         # Entidades, Enums, Interfaces de Repositorio (puro, sin Spring Web)
├── application/    # Casos de Uso, Servicios de Aplicación, DTOs (Request/Response)
└── infrastructure/ # Controladores REST, Implementaciones JPA, Clientes HTTP
```

## 3. Manejo de Excepciones y Respuestas Unificadas
- Toda excepción de negocio debe heredar de `BusinessException` (en `shared.domain`).
- Usar un `@RestControllerAdvice` centralizado para mapear excepciones a `ProblemDetail` (RFC 7807) o `ApiResponse<T>` consistente:
```json
{
  "success": false,
  "timestamp": "2026-10-01T12:00:00Z",
  "correlationId": "uuid-v4",
  "error": {
    "code": "SYSTEM_NOT_REACHABLE",
    "message": "No se pudo establecer conexión con el sistema externo",
    "details": ["Timeout de 3000ms excedido"]
  }
}
```

## 4. Persistencia y Transaccionalidad
- Mantener `@Transactional(readOnly = true)` a nivel de clase de servicio y `@Transactional` solo en métodos de escritura.
- Usar Flyway para migraciones incrementales (`V1__init_schema.sql`).
- Evitar el antipatrón `FetchType.EAGER` en relaciones JPA; usar siempre `FetchType.LAZY` y DTO projections para consultas de alto rendimiento.
