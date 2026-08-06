# Alcance de la Fase I (MVP v0.5.0) — tariff-service

Documento explicativo del alcance, responsabilidad, modelo de dominio, endpoints expuestos e infraestructura del microservicio `tariff-service` durante la **Fase I (MVP v0.5.0)** del sistema de parking inteligente **TARTIS Recon-AI**.

---

## 1. Responsabilidad del Microservicio en Fase I

En la Fase I, `tariff-service` se encarga de la gestión del catálogo de tarifas de aparcamiento:
- **Gestión del Catálogo de Tarifas:** Alta, edición, desactivación y consulta de tarifas clasificadas por tipo de vehículo (`CAR`, `MOTORBIKE`, `CAR_PMR`).
- **Garantía del Invariante (IN-08):** Asegurar que solo exista una única tarifa activa por cada categoría de vehículo.
- **Consulta de Tarifa Vigente:** Exponer la tarifa activa requerida por `stay-service` en las operaciones de cálculo.

---

## 2. Modelo de Dominio (Fase I)

Entidad principal **`Tariff`** con los siguientes atributos:

| Atributo | Tipo | Descripción | Validación / Restricción |
|---|---|---|---|
| `id` | `UUID` | Identificador único universal de la tarifa | Autogenerado (PK) |
| `vehicleType` | `VehicleType` | Categoría de vehículo (`CAR`, `MOTORBIKE`, `CAR_PMR`) | No nulo |
| `baseFee` | `BigDecimal` | Cuota fija de acceso inicial | Requerido $\ge 0$ |
| `minuteRate` | `BigDecimal` | Precio por minuto de estacionamiento | Requerido $\ge 0$ |
| `active` | `Boolean` | Indicador de tarifa activa | Invariante **IN-08** (1 activa por tipo) |

---

## 3. Endpoints REST Expuestos (Fase I)

| Método HTTP | Endpoint | Descripción | Cuerpo / Parámetros | Respuesta Éxito |
|---|---|---|---|---|
| `GET` | `/v1/tariffs` | Listado del catálogo de tarifas | Ninguno | `200 OK` (Lista de `TariffResponse`) |
| `POST` | `/v1/tariffs` | Creación de tarifa por tipo de vehículo | JSON `CreateTariffRequest` | `201 Created` (`TariffResponse`) |
| `GET` | `/v1/tariffs/{tariffId}` | Consulta de tarifa por su UUID | `{tariffId}` (UUID) | `200 OK` (`TariffResponse`) |
| `PUT` | `/v1/tariffs/{tariffId}` | Edición/modificación de tarifa existente | `{tariffId}`, JSON `UpdateTariffRequest` | `200 OK` (`TariffResponse`) |
| `PATCH` | `/v1/tariffs/{tariffId}/deactivate` | Desactivación explícita de tarifa | `{tariffId}` | `200 OK` (`TariffResponse`) |
| `GET` | `/v1/tariffs/current` | Consulta de la tarifa activa por categoría | `vehicleType` | `200 OK` (`TariffResponse`) |

---

## 4. Arquitectura y Persistencia en Fase I

- **Arquitectura Hexagonal:** Adaptador de entrada REST (`TariffRestControllerAdapter`), Casos de Uso (`CreateTariffUseCase`, `GetTariffUseCase`), Adaptador de salida JPA (`TariffPersistenceAdapter`).
- **Base de Datos:** PostgreSQL compartido `parking_dev` en puerto `5432`, esquema `tariff`.

---

## 5. Diferencias Clave respecto a la Fase II (v1.0.0)

1. **Cálculo Avanzado por Tramos Escalonados:** No existe el endpoint `/tariffs/calculate` aplicando cuota fija (0,10 €), cortesía de 10 min, tramos escalonados y redondeo a favor del sistema (**RN-06** a **RN-09**).
2. **Seguridad OAuth2 / Keycloak & Kong:** Sin verificación de Bearer Access Tokens JWT ni roles RBAC (`ADMIN`, `OPERARIO`).
3. **Autoconfiguración JwtDecoder en Prod (SEC-07):** Sin desacoplamiento de beans para perfil `prod`.
