package com.tartis_recon_ai_parking.domain.tariff.exception;

import java.util.UUID;

import com.tartis_recon_ai_parking.domain.tariff.VehicleType;

public class TariffNotFoundException extends RuntimeException {

    public TariffNotFoundException(UUID id) {
        super("Tariff not found with id: " + id);
    }

     public TariffNotFoundException(VehicleType type) {
        super("Tariff not found with type: " + type);
    }

    public TariffNotFoundException(String message) {
        super(message);
    }
}