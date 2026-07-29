package com.tartis_recon_ai_parking.application.tariff.exception;

/** Fallo no transitorio de persistencia: esquema desalineado, SQL invalido. -> 500 */
public class PersistenceFailureException extends RuntimeException {

    public PersistenceFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}