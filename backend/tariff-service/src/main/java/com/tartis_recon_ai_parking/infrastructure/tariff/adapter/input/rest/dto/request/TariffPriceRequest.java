package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request;

import java.util.UUID;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class TariffPriceRequest {

    @NotNull
    private UUID tariffId;

    @NotNull
    private VehicleType type;

    @NotNull
    @PositiveOrZero
    private Integer minutes;

    public TariffPriceRequest() {
    }

    public TariffPriceRequest(UUID tariffId, VehicleType type, Integer minutes) {
        this.tariffId = tariffId;
        this.type = type;
        this.minutes = minutes;
    }

    public UUID getTariffId() { return tariffId; }
    public void setTariffId(UUID tariffId) { this.tariffId = tariffId; }

    public VehicleType getType() { return type; }
    public void setType(VehicleType type) { this.type = type; }

    public Integer getMinutes() { return minutes; }
    public void setMinutes(Integer minutes) { this.minutes = minutes; }

}
