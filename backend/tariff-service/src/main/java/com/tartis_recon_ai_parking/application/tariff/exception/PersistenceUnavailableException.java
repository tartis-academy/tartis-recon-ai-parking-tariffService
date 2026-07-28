package com.tartis_recon_ai_parking.application.tariff.exception;

/** Fallo transitorio de persistencia: BD caida, timeout. -> 503 */
public class PersistenceUnavailableException extends RuntimeException {

    public PersistenceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}