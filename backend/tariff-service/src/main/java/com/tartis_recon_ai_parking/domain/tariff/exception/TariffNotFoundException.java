package com.tartis_recon_ai_parking.domain.tariff.exception;

import java.util.UUID;

public class TariffNotFoundException extends RuntimeException {

    public TariffNotFoundException(UUID id) {
        super("Tariff not found with id: " + id);
    }

    public TariffNotFoundException(String message) {
        super(message);
    }
}