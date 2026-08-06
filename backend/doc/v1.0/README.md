# Alcance de la Fase II (v1.0.0) — tariff-service

Documento explicativo del alcance, responsabilidad, modelo de dominio, endpoints expuestos, cálculo tarifario escalonado, seguridad e infraestructura del microservicio `tariff-service` durante la **Fase II (v1.0.0)** del sistema de parking inteligente **TARTIS Recon-AI**.

---

## 1. Responsabilidad del Microservicio en Fase II

En la Fase II, `tariff-service` incorpora la motorización de cálculo tarifario escalonado consumida síncronamente por `stay-service` en check-out:
- **Cálculo Avanzado de Importe (`/tariffs/calculate`):**
  - **Cuota fija y cortesía (RN-09):** Cuota fija de acceso (0,10 €) y primeros 10 minutos de cortesía (0,00 €).
  - **Tramos escalonados (RN-07):** 0,03 €/min primera hora (11-60 min), 0,02 €/min tramo intermedio (61-1440 min), 0,01 €/min larga estancia (>1440 min).
  - **Acumulación escalonada (RN-08):** El precio acumulado en tramos anteriores se mantiene sin aplicar tarifas retroactivas al saltar de tramo.
  - **Redondeo hacia arriba (RN-06):** Minutos fraccionados redondeados al entero superior a favor del sistema.
- **Invariante IN-08:** Desactivación atómica de la tarifa previa al activar/crear una nueva tarifa para la misma categoría (`CAR`, `MOTORBIKE`, `CAR_PMR`).
- **Seguridad OAuth2 / Keycloak & Kong:** Resource Server para validación de tokens JWT y roles RBAC (`ADMIN`, `OPERARIO`).

---

## 2. Modelo de Dominio y Persistencia (Fase II)

Entidad **`Tariff`**:

| Atributo | Tipo | Descripción | Validación / Restricción |
|---|---|---|---|
| `id` | `UUID` | Identificador único universal de la tarifa | Autogenerado (PK) |
| `vehicleType` | `VehicleType` | Tipo de vehículo (`CAR`, `MOTORBIKE`, `CAR_PMR`) | No nulo |
| `baseFee` | `BigDecimal` | Cuota fija de acceso inicial (0,10 €) | Requerido |
| `minuteRate` | `BigDecimal` | Precio por minuto del tramo base | Requerido |
| `active` | `Boolean` | Estado activo (Garantía **IN-08**) | Invariante de 1 activa |
| `version` | `Long` | Control de concurrencia optimista | Managed por JPA |

---

## 3. Endpoints Expuestos y Matriz de Roles (Fase II)

| Método HTTP | Endpoint | Descripción | Rol Keycloak Requerido |
|---|---|---|---|
| `GET` | `/v1/tariffs` | Listado del catálogo de tarifas | `ADMIN`, `OPERARIO` |
| `POST` | `/v1/tariffs` | Creación de nueva tarifa (desactiva previa **IN-08**) | `ADMIN` |
| `GET` | `/v1/tariffs/{tariffId}` | Consulta de tarifa por UUID | `ADMIN`, `OPERARIO` |
| `PUT` | `/v1/tariffs/{tariffId}` | Edición de tarifa | `ADMIN` |
| `PATCH` | `/v1/tariffs/{tariffId}/deactivate` | Desactivación explícita de tarifa | `ADMIN` |
| `GET` | `/v1/tariffs/current` | Consulta de tarifa activa por categoría | `ADMIN`, `OPERARIO` |
| `POST` | `/v1/tariffs/calculate` | **Cálculo de importe por tramos (RN-06..09)** | `ADMIN`, `OPERARIO` |

---

## 4. Arquitectura y Seguridad (Fase II)

- **Cálculo de Tarifas Escalonado:** Algoritmo en dominio que desacopla la lógica de tramos acumulados del controlador HTTP.
- **Autoconfiguración JwtDecoder (SEC-07):** Solución de arranque en prod resolviendo JWK Set URI (`KEYCLOAK_JWK_SET_URI`).
- **Base de Datos:** Postgres dedicado `tariff_db` en puerto `5435` (perfil `prod`), migraciones Flyway `V1__init.sql`.
- **Formato Común de Errores RFC 7807 (SEC-11):** Devolución de `ProblemDetail` / `ErrorResponse`.
