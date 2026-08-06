# Changelog

All notable changes to the `tariff-service` microservice will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-08-04

### Added
- **Integración con Keycloak & Spring Security:** Configuración de OAuth2 Resource Server para la validación de Bearer Access Tokens emitidos por Keycloak y resolución de claves JWK (`KEYCLOAK_JWK_SET_URI`).
- **Enrutamiento por API Gateway (Kong):** Enrutamiento centralizado y validación de tokens JWT en el perímetro a través de Kong.
- **Cálculo Avanzado de Importe (`/tariffs/calculate`):** Implementado endpoint de cálculo de precio consumido síncronamente por `stay-service` en check-out, aplicando:
  - **RN-09:** Cuota fija de acceso (0,10 €) y 10 minutos de cortesía (0,00 €).
  - **RN-07:** Tramos escalonados (0,03 €/min primera hora, 0,02 €/min tramo 61-1440 min, 0,01 €/min >1440 min).
  - **RN-08:** Acumulación escalonada no retroactiva al cruzar de tramo.
  - **RN-06:** Redondeo de minutos hacia arriba a favor del sistema.
- **Garantía del Invariante IN-08:** Lógica en dominio para asegurar una única tarifa activa por tipo de vehículo, desactivando atómicamente la previa al crear/activar una nueva.
- **Soporte para Tarifa PMR:** Añadida la categoría `CAR_PMR` en la configuración de tarifas.
- **Trazabilidad Distribuida & Logging (GW-06):** Inclusión de `CorrelationIdFilter`, `RequestIdentityFilter` y `RequestLoggingFilter` inyectando `correlationId`, `userName` y `clientId` en el MDC.

### Changed
- **Formato Común de Errores (SEC-11 / RFC 7807):** Estandarización de respuestas de error devolviendo `ProblemDetail` / `ErrorResponse` uniforme.
- **Control de Acceso basado en Roles (RBAC):** Restricción de endpoints según matriz `SEC-03` (`ADMIN` para creación, modificación y desactivación de tarifas; `ADMIN`/`OPERARIO` para consultas y cálculo).
- **Base de Datos Dedicada:** Perfil `prod` con PostgreSQL dedicada en puerto 5435.

### Fixed
- **Autoconfiguración JwtDecoder en Prod (SEC-07):** Resuelto el fallo de arranque en perfil `prod` desacoplando la configuración del bean de decodificación e inyectando las URIs del emisor e emisor JWK desde variables de entorno.
- **Manejo de Respuestas de Autenticación (401 / 403):** Restaurada la emisión del encabezado `WWW-Authenticate` en respuestas 401.

### Security
- **Protección con `@PreAuthorize`:** Control de acceso en adaptadores REST.
- **Escaneo Continuo de Vulnerabilidades:** Integración con Trivy (`docker-scan`) en el pipeline de CI/CD.

## [0.5.0] - 2026-07-25

### Added
- **MVP Inicial de `tariff-service`:** Implementación inicial de la arquitectura hexagonal para la gestión de tarifas.
- **Endpoints REST Síncronos:**
  - `GET /v1/tariffs`: Consulta de catálogo de tarifas.
  - `POST /v1/tariffs`: Creación de tarifas por tipo de vehículo.
  - `GET /v1/tariffs/{tariffId}`: Consulta por UUID.
  - `PUT /v1/tariffs/{tariffId}`: Edición de tarifas.
  - `PATCH /v1/tariffs/{tariffId}/deactivate`: Desactivación de tarifas.
  - `GET /v1/tariffs/current`: Consulta de tarifa activa por categoría.
- **Persistencia PostgreSQL:** Configuración JPA con esquema `tariff`.
- **Contrato OpenAPI:** Especificación en `openapi.yml`.

[1.0.0]: https://github.com/tartis-academy/tartis-recon-ai-parking-tariffService/compare/v0.5.0...v1.0.0
[0.5.0]: https://github.com/tartis-academy/tartis-recon-ai-parking-tariffService/releases/tag/v0.5.0
