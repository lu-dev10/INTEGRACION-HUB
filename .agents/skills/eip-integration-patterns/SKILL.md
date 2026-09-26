---
name: eip-integration-patterns
description: Guía de implementación de Enterprise Integration Patterns (EIP) para el motor de integración y orquestación. Úsalo al desarrollar el motor de ejecución, particionamiento de lotes (Splitter), correlación y reintentos.
---

# Skill: Enterprise Integration Patterns (EIP)

Este skill define los patrones de integración empresarial que gobiernan el motor de ejecución (`execution engine`) de **Integration Hub**.

## 1. Patrones Clave Implementados

### A. Splitter Pattern (Particionador de Cargas)
- **Problema:** El sistema origen devuelve un array JSON de 100 o 1,000 registros, pero el destino espera un único objeto por invocación.
- **Implementación:**
  1. Detectar si el payload origen es `JsonNode.isArray()`.
  2. Iterar cada elemento como un mensaje unitario independiente.
  3. Ejecutar el pipeline de transformación y despacho por cada elemento sin bloquear el resto si uno falla.

### B. Message Translator (Traductor de Mensajes)
- **Problema:** La estructura del sistema A (`{"codigo": "P1"}`) no coincide con el sistema B (`{"product_code": "P1"}`).
- **Implementación:**
  - Aplicar mapeo campo por campo con soporte de notación JsonPath (`$.datos.cliente`).
  - Encadenar estrategias de transformación (`DIRECT`, `TRIM`, `NUMBER`, etc.).

### C. Correlation Identifier (ID de Correlación)
- **Problema:** Trazabilidad en llamadas distribuidas a múltiples sistemas externos.
- **Implementación:**
  - Generar un UUID v4 por cada ejecución (`Execution.correlationId`).
  - Inyectar el header `X-Correlation-ID: <UUID>` en todas las peticiones salientes hacia sistemas externos.
  - Almacenar el ID en los logs estructurados (MDC de SLF4J).

### D. Dead Letter Channel & Partial Retry (Manejo de Errores Quirúrgico)
- **Problema:** Si 3 de 100 registros fallan con HTTP 400 o 500, no se debe reprocesar todo el lote para evitar duplicados.
- **Implementación:**
  - Persistir cada registro en `ExecutionRecord` con estado `SUCCESS` o `FAILED`.
  - El mecanismo de reintento (`retryFailed`) consulta únicamente los registros con estado `FAILED` de la ejecución asociada y los vuelve a someter al pipeline.

### E. Idempotency Key (Garantía de Entrega Única)
- Enviar el header `X-Idempotency-Key: <correlationId>-<recordIndex>` para asegurar que el sistema de destino no duplique transacciones financieras en caso de reintentos.
