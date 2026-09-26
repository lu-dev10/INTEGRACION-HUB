# Documentación Oficial del Proyecto — Integration Hub

Bienvenido a la base de conocimiento y especificación de ingeniería de **Integration Hub**. Aquí se encuentran formalizados todos los artefactos de diseño, arquitectura, casos de uso y gestión ágil bajo metodología Scrum.

---

## 📚 Índice de Documentación

| Documento | Descripción |
| :--- | :--- |
| **[01. Ficha Técnica](01-ficha-tecnica.md)** | Stack tecnológico (Java 21, Spring Boot 3, Angular, PostgreSQL), arquitectura Monolito Modular, diagramas C4 y Requisitos No Funcionales (NFR). |
| **[02. Especificación de Módulos](02-modulos.md)** | Desglose modular de componentes (`auth`, `system`, `discovery`, `mapping`, `transformation`, `execution`, `audit`, `mock`), patrones de diseño aplicados (Strategy, Pipeline, Splitter EIP). |
| **[03. Casos de Uso](03-casos-de-uso.md)** | Especificación detallada de los 14 Casos de Uso del sistema (CU-001 al CU-014), flujos principales, alternativos y de excepción. |
| **[04. Metodología Scrum](04-metodologia-scrum.md)** | Marco de trabajo ágil: Definition of Ready (DoR), Definition of Done (DoD), Product Backlog por Épicas, User Stories con sintaxis Gherkin (Given-When-Then) y Plan de Sprints. |

---

## 🧠 Catálogo de Skills Especializados (`.agents/skills/`)

Para guiar la implementación técnica con los mejores estándares, se crearon los siguientes skills en el repositorio:
1. **[`spring-boot-java21`](../.agents/skills/spring-boot-java21/SKILL.md):** Arquitectura modular, Virtual Threads de Java 21, JPA limpio y control unificado de excepciones.
2. **[`eip-integration-patterns`](../.agents/skills/eip-integration-patterns/SKILL.md):** Enterprise Integration Patterns (Splitter, Translator, Correlation ID, reintentos parciales e idempotencia).
3. **[`openapi-discovery`](../.agents/skills/openapi-discovery/SKILL.md):** Parser de especificaciones OpenAPI v3 y Swagger v2 con resolución de referencias `$ref`.
4. **[`data-mapping-transformations`](../.agents/skills/data-mapping-transformations/SKILL.md):** Manejo de JsonPath y Strategy Pattern para transformadores de datos.
5. **[`angular-clean-ui`](../.agents/skills/angular-clean-ui/SKILL.md):** Arquitectura Angular 18+ (Standalone, Signals, Reactive Forms).
6. **[`ui-ux-design-system`](../.agents/skills/ui-ux-design-system/SKILL.md):** Design System visual, tokens semánticos (Dark Theme, Glassmorphism), tipografía, mapeador visual de campos e inspector de payloads.

---

## 🎯 Objetivo Inmediato (Sprint 1)
- Levantar infraestructura con **Docker Compose** (PostgreSQL 16).
- Estructurar el proyecto Maven con **Spring Boot 3.3.x** y **Java 21**.
- Implementar **Health Check**, módulo **auth** con JWT y módulo **system** con prueba de conectividad (Ping).
