package com.tartis_recon_ai_parking.domain.tariff.exception;

/**
 * Una fila de BD no satisface los invariantes de dominio (precio <= 0,
 * nombre nulo...). Es un fallo del servidor, NO del cliente: por eso no
 * puede acabar reutilizando InvalidTariffException, que mapea a 400.
 */
public class CorruptedTariffDataException extends RuntimeException {
    public CorruptedTariffDataException(String message, Throwable cause) { super(message, cause); }
}