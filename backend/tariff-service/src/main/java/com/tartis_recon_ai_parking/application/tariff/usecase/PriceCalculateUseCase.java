package com.tartis_recon_ai_parking.application.tariff.usecase;

import java.math.BigDecimal;
import java.util.UUID;

import com.tartis_recon_ai_parking.application.tariff.dto.PriceTransferDTO;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffNotFoundException;

import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import com.tartis_recon_ai_parking.domain.tariff.exception.TariffConstraintException;

public class PriceCalculateUseCase {
    
    private final TariffPersistence tariffPersistence;

    public PriceCalculateUseCase(TariffPersistence tariffPersistence) {
        this.tariffPersistence = tariffPersistence;
    }

    public PriceTransferDTO execute(UUID tariffId, VehicleType type, int minutes){
        
        Tariff tariff = tariffPersistence.findById(tariffId)
                .orElseThrow(() -> new TariffNotFoundException(tariffId));
        
        if (tariff.getType() != type) {
            throw new TariffConstraintException("The provided tariff ID does not match the vehicle type of the stay.");
        }
        
        //Cálculo del precio -> Precio = precioBase + (precioPorMinuto * numMinutos)
        BigDecimal price = tariff.getBasePrice()
        .add(tariff.getPricePerMinute().multiply(BigDecimal.valueOf(minutes)));

        PriceTransferDTO result = new PriceTransferDTO(price);
        
        return result;
    }

}
