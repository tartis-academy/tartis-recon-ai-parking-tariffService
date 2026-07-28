package com.tartis_recon_ai_parking.domain.tariff.exception;

import java.util.UUID;

/**
 * Se lanza cuando dos operaciones concurrentes intentan modificar la misma
 * tarifa a la vez (condicion de carrera, TAR-1780). El patron read-modify-write
 * de los casos de uso (findById -> mutar -> save) es vulnerable a lost updates
 * si dos administradores operan sobre el mismo id casi simultaneamente; esta
 * excepcion representa el conflicto detectado por el control de version
 * optimista en el momento del save.
 */
public class TariffConcurrentModificationException extends RuntimeException {

    public TariffConcurrentModificationException(UUID id) {
        super("Tariff with id " + id + " was modified concurrently by another operation. Please retry with the latest data.");
    }
}