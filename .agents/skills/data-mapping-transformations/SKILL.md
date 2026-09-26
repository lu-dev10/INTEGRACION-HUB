---
name: data-mapping-transformations
description: Guía de diseño para mapeo campo a campo, JsonPath y transformaciones con el patrón Strategy. Úsalo al construir el motor de transformación de datos y validación de esquemas.
---

# Skill: Data Mapping & Transformations Engine

Este skill establece las reglas de extracción, conversión y ensamble de payloads JSON en **Integration Hub**.

## 1. Extracción con JsonPath
Para soportar campos anidados sin código acoplado, se utiliza la librería Jayway JsonPath:
```java
// Ejemplo: sourceField = "cliente.nombre" o "$.cliente.nombre"
Configuration conf = Configuration.defaultConfiguration()
    .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL, Option.SUPPRESS_EXCEPTIONS);

DocumentContext jsonContext = JsonPath.using(conf).parse(rawSourceJson);
Object extractedValue = jsonContext.read(normalizePath(sourceField));
```

## 2. Patrón de Diseño Strategy para Transformaciones
Definir una interfaz común inyectada por Spring mediante `@Component`:

```java
public interface TransformationStrategy {
    TransformationType getType();
    Object transform(Object value, Map<String, Object> parameters);
}
```

### Catálogo de Estrategias V1
1. `DIRECT`: Retorna el valor sin alterar.
2. `TRIM`: Si es String, aplica `.trim()`. Si es null, retorna null.
3. `UPPERCASE`: Convierte a mayúsculas con `Locale.ROOT`.
4. `LOWERCASE`: Convierte a minúsculas con `Locale.ROOT`.
5. `NUMBER`: Convierte String/Integer a `BigDecimal` o `Double`, tolerando comas o puntos.
6. `STRING`: Convierte cualquier tipo primitivo o numérico a `String`.
7. `DATE_FORMAT`: Recibe `sourceFormat` (ej. `yyyy-MM-dd`) y `targetFormat` (ej. `dd/MM/yyyy`) y reformatea usando `java.time.format.DateTimeFormatter`.

## 3. Ensamblado del JSON Destino
Usar Jackson `ObjectNode` para construir dinámicamente el payload destino:
```java
ObjectMapper mapper = new ObjectMapper();
ObjectNode targetPayload = mapper.createObjectNode();

for (FieldMapping rule : mappingRules) {
    Object rawValue = extract(sourceJson, rule.getSourceField());
    Object transformedValue = strategyRegistry.get(rule.getTransformationType())
                                              .transform(rawValue, rule.getParameters());
    setField(targetPayload, rule.getTargetField(), transformedValue);
}
```
