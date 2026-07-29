package com.tartis_recon_ai_parking.domain.tariff.exception;

/** Viola la unicidad de nombre de tarifa. Se traduce a 409 Conflict. */
public class TariffAlreadyExistsException extends RuntimeException {
    public TariffAlreadyExistsException(String message) { super(message); }
}