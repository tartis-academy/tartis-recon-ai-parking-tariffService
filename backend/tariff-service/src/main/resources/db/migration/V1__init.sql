-- RECON-812: migracion baseline de Flyway, sustituye al schema.sql que se
-- montaba como init script de Postgres. Debe reflejar exactamente
-- TariffEntity.
CREATE TABLE tariffs (
    unique_id        UUID PRIMARY KEY,
    name             VARCHAR(255) NOT NULL UNIQUE,
    type             VARCHAR(20) NOT NULL,
    price_per_minute NUMERIC NOT NULL,
    base_price       NUMERIC NOT NULL,
    active           BOOLEAN NOT NULL
);
