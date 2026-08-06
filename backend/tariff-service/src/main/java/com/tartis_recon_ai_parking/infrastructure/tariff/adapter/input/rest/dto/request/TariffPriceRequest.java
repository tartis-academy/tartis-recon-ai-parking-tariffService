package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public class TariffPriceRequest {

    @NotNull
    UUID tariffId;

    @NotNull
    int minutes;

    public TariffPriceRequest() {
    }

    public TariffPriceRequest(UUID tariffId, int minutes) {
        this.tariffId = tariffId;
        this.minutes = minutes;
    }

    public UUID getTariffId() { return tariffId; }
    public void setTariffId(UUID tariffId) { this.tariffId = tariffId; }

    public int getMinutes() { return minutes; }
    public void setMinutes(int minutes) { this.minutes = minutes; }

}
