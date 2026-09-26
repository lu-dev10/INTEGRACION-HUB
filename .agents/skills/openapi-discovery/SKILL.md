---
name: openapi-discovery
description: Guía técnica para inspeccionar, descargar y parsear especificaciones OpenAPI v3 y Swagger v2. Úsalo al implementar el módulo de descubrimiento de APIs, extracción de esquemas y resolución de referencias $ref.
---

# Skill: OpenAPI & Swagger Discovery Engine

Este skill documenta cómo implementar la inspección y análisis automático de APIs externas dentro de **Integration Hub**.

## 1. Librería Principal
Utilizar el parser oficial de Swagger:
```xml
<dependency>
    <groupId>io.swagger.parser.v3</groupId>
    <artifactId>swagger-parser</artifactId>
    <version>2.1.22</version>
</dependency>
```

## 2. Estrategia de Descubrimiento de URLs
Al ingresar un sistema con base URL (ej. `http://api.sistema.com`), el motor prueba en orden las siguientes rutas comunes:
1. `${baseUrl}/v3/api-docs` (Estándar Springdoc OpenAPI 3)
2. `${baseUrl}/swagger.json` (Estándar Swagger 2 clásico)
3. `${baseUrl}/openapi.json` (Estándar FastAPI / Express OpenAPI)
4. `${baseUrl}/api-docs`

## 3. Resolución de Referencias `$ref`
`OpenAPIV3Parser` cuenta con opciones para aplanar y resolver esquemas recursivos automáticamente:
```java
ParseOptions options = new ParseOptions();
options.setResolve(true); // Resuelve automáticamente todos los $ref hacia components/schemas
options.setResolveFully(true);

SwaggerParseResult result = new OpenAPIV3Parser().readLocation(specUrl, null, options);
OpenAPI openAPI = result.getOpenAPI();
```

## 4. Extracción de Endpoints
Por cada entrada en `openAPI.getPaths()`:
1. Iterar sobre los métodos HTTP (`GET`, `POST`, `PUT`, `DELETE`, `PATCH`).
2. Extraer:
   - **Path:** `/api/sales`
   - **HttpMethod:** `GET`
   - **Summary / Description:** Descripción legible para la interfaz.
   - **Request Schema:** Esquema JSON del `requestBody` (si aplica para `POST`/`PUT`).
   - **Response Schema:** Esquema JSON de la respuesta `200` o `201`.
3. Persistir en la entidad de dominio `Endpoint` serializando los schemas como JSON estándar para el configurador de mapeo.
