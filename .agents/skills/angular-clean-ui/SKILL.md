---
name: angular-clean-ui
description: Guía de arquitectura frontend para Angular 18+, componentes Standalone, Signals, Reactive Forms y diseño visual premium. Úsalo al desarrollar las interfaces del dashboard, gestor de sistemas, mapeador visual y monitor de ejecuciones.
---

# Skill: Angular 18+ Clean UI & Architecture

Este skill define las directrices para la construcción del frontend de **Integration Hub** en Angular 18+.

## 1. Principios Técnicos
- **Standalone Components:** Todos los componentes, directivas y pipes deben ser `standalone: true`. Sin `NgModule`.
- **Gestión de Estado Reactivo con Signals:** Utilizar `signal()`, `computed()` y `effect()` para el estado local y la reactividad de la interfaz.
- **Reactive Forms:** Emplear `FormBuilder`, `FormGroup` y `FormControl` tipados para la captura de credenciales y reglas de mapeo.
- **Inyección de Dependencias Moderna:** Usar `inject(HttpClient)` en lugar de inyección por constructor.

## 2. Sistema de Diseño Visual (CSS / Tokens)
- **Modo Oscuro / Tema Profesional:** Fondo oscuro sofisticado (`#0f172a`, `#1e293b`), contrastes claros (`#f8fafc`), acentos en índigo/azul vibrante (`#6366f1`, `#38bdf8`) y estados semánticos (verde `#10b981`, amarillo `#f59e0b`, rojo `#ef4444`).
- **Glassmorphism:** Tarjetas con fondos translúcidos y bordes sutiles:
  ```css
  background: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  ```
- **Micro-animaciones:** Transiciones suaves de 0.2s en hovers de botones y tarjetas.

## 3. Módulos y Pantallas Frontend
1. **Dashboard (`/dashboard`):**
   - Tarjetas de KPIs (Integraciones, Ejecuciones de Hoy, Tasa de Éxito, Latencia promedio).
   - Tabla de últimas ejecuciones con badges dinámicos de estado (`SUCCESS`, `PARTIAL_SUCCESS`, `FAILED`).
2. **Sistemas Externos (`/systems`):**
   - Lista en cuadrícula de sistemas con botón "Probar Conexión (Ping)" interactivo.
   - Botón "Descubrir API" con animación de escaneo.
   - Vista de endpoints descubiertos (acordeón expandible con métodos HTTP coloreados).
3. **Constructor de Mappings (`/integrations/new`):**
   - Panel de dos columnas con selector visual de origen y destino.
   - Selector de transformación por campo con vista previa inmediata.
4. **Monitor Forense de Ejecuciones (`/executions/:id`):**
   - Filtro por registros exitosos / fallidos.
   - Modal con inspector de payloads: Request Original, Transformado, Response de Destino y Error HTTP.
   - Botón de acción: "Reintentar fallidos".
