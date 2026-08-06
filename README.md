# tartis-recon-ai-parking — tariff-service

## 1. Responsabilidad del microservicio

`tariff-service` es el microservicio responsable de la configuración del catálogo de tarifas por tipo de vehículo y del cálculo del importe exacto a cobrar por las estancias en el parking **TARTIS Recon-AI**. Sus responsabilidades principales incluyen:

- **Gestión del Catálogo de Tarifas:** Creación, modificación, desactivación y consulta de tarifas por tipo de vehículo (`CAR`, `CAR_PMR`, `MOTORBIKE`).
- **Garantía del Invariante IN-08:** Garantiza que en todo momento exista como máximo **una única tarifa activa** por categoría de vehículo. Al crear o activar una tarifa nueva para una categoría, la tarifa anterior queda desactivada de forma atómica en la misma transacción.
- **Cálculo del Importe de Estancia (`/tariffs/calculate`):** Invocado síncronamente por `stay-service` durante el check-out para calcular el precio consumido según la tarifa activa de la categoría:
  $$\text{Importe Total} = \text{basePrice} + (\text{pricePerMinute} \times \text{minutos})$$
- **Emisión de Eventos de Dominio:** Publicación de eventos de cambio de tarifa (`TariffChangedEvent` / `SpringTariffChangedEvent`) hacia RabbitMQ al modificar o desactivar precios.

---

## 2. Endpoints expuestos

Todos los endpoints requieren autenticación mediante Bearer Access Token (emitido por Keycloak), exceptuando las sondas públicas de salud.

| Método | Endpoint | Descripción | Roles Autorizados (RBAC SEC-03) | Respuesta Exitosa |
|---|---|---|---|---|
| `GET` | `/v1/tariffs` | Listado paginado de tarifas (filtros por `vehicleType` y `active`) | `ADMIN`, `OPERARIO` | `200 OK` (`List<TariffResponse>`) |
| `POST` | `/v1/tariffs` | Crea y activa una tarifa (desactiva la anterior según IN-08) | `ADMIN` | `201 Created` (`TariffResponse`) |
| `GET` | `/v1/tariffs/{tariffId}` | Obtiene el detalle de una tarifa por su UUID | `ADMIN`, `OPERARIO` | `200 OK` (`TariffResponse`) |
| `PUT` | `/v1/tariffs/{tariffId}` | Modifica los datos de una tarifa existente | `ADMIN` | `200 OK` (`TariffResponse`) |
| `PATCH` | `/v1/tariffs/{tariffId}/deactivate` | Desactiva una tarifa existente | `ADMIN` | `200 OK` (`TariffResponse`) |
| `GET` | `/v1/tariffs/current` | Consulta la tarifa actualmente activa para un tipo de vehículo (IN-08) | `ADMIN`, `OPERARIO` | `200 OK` (`TariffResponse`) |
| `POST` | `/v1/tariffs/calculate` | **Calcula el importe de una estancia síncronamente** | `ADMIN`, `OPERARIO` | `200 OK` (`PriceResponse`) |
| `GET` | `/actuator/health` | Probes de salud del servicio (Liveness / Readiness) | Público | `200 OK` |

---

## 3. Casos de Uso (Arquitectura Hexagonal)

Los casos de uso encapsulan las reglas de negocio del dominio de tarifas:

- **`CreateTariffUseCase`:** Registra y activa una tarifa desactivando atómicamente cualquier previa del mismo tipo (IN-08).
- **`ActivateTariffUseCase`:** Activa una tarifa existente y desactiva la previa.
- **`DeactivateTariffUseCase`:** Desactiva una tarifa concreta.
- **`GetActiveTariffByTypeUseCase`:** Busca la tarifa vigente (`active = true`) para un tipo de vehículo.
- **`GetTariffUseCase`:** Consulta una tarifa por su identificador UUID.
- **`ListTariffsUseCase`:** Retorna el catálogo completo de tarifas.
- **`PriceCalculateUseCase`:** Ejecuta la fórmula de cálculo de importe tomando la tarifa activa correspondiente al tipo de vehículo.
- **`UpdateTariffUseCase`:** Actualiza los valores de precio base y precio por minuto de una tarifa.

### Puertos de Dominio:
- **Puerto de Entrada:** `TariffRestAdapter` (`POST /v1/tariffs/calculate`, endpoints CRUD).
- **Puertos de Salida:** `TariffPersistence` (`TariffPersistenceAdapter`), `TariffEventPublisher` (`TariffEventPublisherAdapter`).

---

## 4. Eventos publicados y consumidos

- **Eventos publicados en RabbitMQ:**
  - **`TariffChangedEvent` / `SpringTariffChangedEvent`:** Publicado en la Exchange `parking-events-exchange` con routing key `tariff-changed-v1` al crear, activar, modificar o desactivar una tarifa (`TariffEventPublisherAdapter`).
- **Eventos consumidos de RabbitMQ:** Ninguno.

---

## 5. Variables de entorno

| Variable | Descripción | Valor por defecto (Dev) | Perfil / Uso |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil activo de Spring Boot | `dev` | `dev` / `prod` |
| `SERVER_PORT` | Puerto HTTP del servicio | `8083` | Dev / Prod |
| `DB_HOST` | Host de la BD compartida de desarrollo | `localhost` | Dev |
| `DB_PORT` | Puerto de la BD compartida | `5432` | Dev |
| `DB_NAME` | Nombre de la BD de desarrollo | `parking_dev` | Dev |
| `DB_USER` | Usuario de la BD de desarrollo | `parking_dev` | Dev |
| `DB_PASSWORD` | Contraseña de la BD de desarrollo | `change.me` | Dev |
| `TARIFF_DB_HOST` | Host de la BD dedicada de tarifas | `parking-tariff-postgres` | Prod / Aislado |
| `TARIFF_DB_PORT` | Puerto del host para la BD dedicada | `5435` (externo) / `5432` (interno) | Prod / Aislado |
| `TARIFF_DB_NAME` | Nombre de la BD dedicada | `tariff_db` | Prod / Aislado |
| `TARIFF_DB_USER` | Usuario de la BD dedicada | `tariff_user` | Prod / Aislado |
| `TARIFF_DB_PASSWORD` | Contraseña de la BD dedicada | `tariff_pass` | Prod / Aislado |
| `KEYCLOAK_ISSUER_URI` | URI del emisor de Keycloak (Issuer URI) | `http://localhost:8180/realms/parking` | Dev / Prod |
| `KEYCLOAK_JWK_SET_URI` | URI del conjunto de claves JWK de Keycloak | `http://localhost:8180/realms/parking/protocol/openid-connect/certs` | Dev / Prod |

---

## 6. Ejecución de forma aislada

Para ejecutar y probar `tariff-service` de forma independiente sin depender del resto de microservicios:

1. **Opción 1: Entorno de Desarrollo (Perfil `dev`)**
   Navegar a la carpeta del microservicio y arrancar con Maven:
   ```bash
   cd backend/tariff-service
   mvn spring-boot:run
   ```
   *El servicio se conectará al esquema `tariff` del Postgres compartido.*

2. **Opción 2: Base de Datos Dedicada (Perfil `prod` / Contenedores Aislados)**
   Para ejecutar contra una base de datos PostgreSQL exclusiva en puerto `5435`:
   ```bash
   cd backend/tariff-service
   cp .env.example .env
   docker compose up -d
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

---

## 7. Migraciones de base de datos (Flyway)

El esquema ya no se crea a mano ni con un `schema.sql` montado como init script: `V1__init.sql` (en `backend/tariff-service/src/main/resources/db/migration`) es la baseline, y Flyway la aplica solo al arrancar la app contra la BD dedicada (perfil `prod`). En dev, Flyway está desactivado (`spring.flyway.enabled=false` en `application-dev.properties`): el Postgres compartido con 5 schemas sigue gestionado por `ddl-auto=update`, fuera del alcance de esta migración.

Para añadir un cambio de esquema: crea `V2__descripcion.sql` (nunca edites `V1__init.sql` una vez desplegado) en la misma carpeta, con el DDL nuevo. Flyway lo detecta y lo aplica en el siguiente arranque.

---

## 8. Escaneo de imagen (Trivy)

El job `docker-scan` de la CI construye la imagen final del Dockerfile y la escanea con [Trivy](https://trivy.dev/). El informe completo (`CRITICAL` + `HIGH`) se publica siempre en la pestaña **Security** del repo; solo una vulnerabilidad `CRITICAL` hace fallar el job.

Si una `CRITICAL` no tiene fix disponible todavía y hay que aceptar el riesgo de forma consciente, se ignora explícitamente añadiendo su CVE a un `.trivyignore` en la raíz del repo (no existe ninguno hoy).