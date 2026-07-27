package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TariffEntityTest {

    @Test
    @DisplayName("El constructor con todos los argumentos debe fijar cada campo")
    void allArgsConstructorSetsEveryField() {
        UUID id = UUID.randomUUID();

        TariffEntity entity = new TariffEntity(id, "Standard", VehicleType.CAR,
                new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        assertThat(entity.getUniqueId()).isEqualTo(id);
        assertThat(entity.getName()).isEqualTo("Standard");
        assertThat(entity.getType()).isEqualTo(VehicleType.CAR);
        assertThat(entity.getPricePerMinute()).isEqualTo(new BigDecimal("0.05"));
        assertThat(entity.getBasePrice()).isEqualTo(new BigDecimal("2.0"));
        assertThat(entity.isActive()).isTrue();
    }
}
