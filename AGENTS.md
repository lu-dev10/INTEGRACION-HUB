# Instrucciones y Reglas de Desarrollo — Integration Hub

## 1. Modo Autónomo y Ejecución Directa
- **No pedir confirmación previa paso a paso**: Cuando el usuario pida realizar una tarea ("hazlo", "procede", "inicia", etc.), realiza directamente las modificaciones, creación de archivos, compilaciones y comandos necesarios sin preguntar antes si puede proceder.
- **Aceptación de cambios**: El usuario prefiere ejecución directa; aplica los cambios y muestra el resultado final.
- **Excepción**: Solo detenerse y preguntar si hay ambigüedad crítica en los requisitos o si una acción implica pérdida irreversible de datos de producción.

## 2. Metodología Scrum y Estándares de Ingeniería
- **Alineación con el Product Backlog**: Cada desarrollo debe apegarse a las Historias de Usuario (US), Épicas y Criterios de Aceptación (Gherkin) definidos en [`docs/04-metodologia-scrum.md`](docs/04-metodologia-scrum.md).
- **Definition of Done (DoD)**: Todo código entregado debe:
  1. Compilar limpiamente sin errores ni advertencias graves.
  2. Implementar pruebas unitarias / de integración correspondientes.
  3. Respetar la arquitectura Monolito Modular documentada en [`docs/02-modulos.md`](docs/02-modulos.md).
  4. Mantener la documentación técnica sincronizada en [`docs/`](docs/).
- **Stack Obligatorio**:
  - Backend: Java 21 (LTS) + Spring Boot 3.3.x + Maven + PostgreSQL.
  - Frontend: Angular 18+ (Standalone components, reactive forms).
  - Infraestructura: Docker & Docker Compose.
