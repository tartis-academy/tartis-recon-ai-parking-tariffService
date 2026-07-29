-- TAR-1780: TariffEntity declara @Version Long version para el control de
-- concurrencia optimista, pero V1__init.sql nunca creo la columna. Sin esto
-- Hibernate falla al arrancar con ddl-auto=validate (application-prod.properties).
--
-- Nullable porque las filas existentes no tienen version: Hibernate trata
-- NULL como "sin version todavia" y arranca el contador en el primer UPDATE.
ALTER TABLE tariffs
    ADD COLUMN version BIGINT;