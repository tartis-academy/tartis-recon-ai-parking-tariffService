package com.tartis_recon_ai_parking.application.tariff.exception;

/** Conflicto de concurrencia: deadlock, lock no adquirido, lock optimista. -> 409 */
public class ConcurrentModificationConflictException extends RuntimeException {

    public ConcurrentModificationConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}