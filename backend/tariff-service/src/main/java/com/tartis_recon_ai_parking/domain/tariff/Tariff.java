package com.tartis_recon_ai_parking.domain.tariff;

import java.math.BigDecimal;
import java.util.UUID;

import com.tartis_recon_ai_parking.domain.tariff.exception.InvalidTariffException;

public class Tariff {

    private UUID uniqueId;

    private String name;
    private VehicleType type;
    private BigDecimal pricePerMinute;
    private BigDecimal basePrice;
    private boolean active;

    public static Tariff create(String name, VehicleType type, BigDecimal pricePerMinute, BigDecimal basePrice) {
        return new Tariff(UUID.randomUUID(), name, type, pricePerMinute, basePrice, true);
    }

    public static Tariff reconstruct(UUID id, String name, VehicleType type, BigDecimal pricePerMinute, BigDecimal basePrice, boolean active) {
        return new Tariff(id, name, type, pricePerMinute, basePrice, active);
    }

    private Tariff(UUID id, String name, VehicleType type, BigDecimal pricePerMinute, BigDecimal basePrice, boolean active) {

        validateData(name, type, pricePerMinute, basePrice);
        
        this.uniqueId = id;
        this.name = name;
        this.type = type;
        this.pricePerMinute = pricePerMinute;
        this.basePrice = basePrice;
        this.active = active;
    }

    private void validateData(String name, VehicleType type, BigDecimal pricePerMinute, BigDecimal basePrice){
        //Following line will throw an exception if the variable is null or only contains blank characters.
        if (name == null || name.isBlank()) throw new InvalidTariffException("Tariff name is null.");
        if (type == null) throw new InvalidTariffException("Vehicle type is null.");

        //BigDecimal's num.compareTo(arg) results:
        //                  -1 -> num is lower than arg
        //                   0 -> num is equal to arg
        //                   1 -> num is greater than arg
        if (pricePerMinute.compareTo(BigDecimal.ZERO) <= 0) throw new InvalidTariffException("PricePerMinute must be greater than 0.");
        if (basePrice.compareTo(BigDecimal.ZERO) < 0) throw new InvalidTariffException("BasePrice must be a positive number.");

    }
    
    public Tariff update(String name, BigDecimal pricePerMinute, BigDecimal basePrice){
        return new Tariff(this.uniqueId, name, this.type, pricePerMinute, basePrice, this.active);
    }

    public Tariff activate() {
        return new Tariff(this.uniqueId, this.name, this.type, this.pricePerMinute, this.basePrice, true);
    }

    public Tariff deactivate() {
        return new Tariff(this.uniqueId, this.name, this.type, this.pricePerMinute, this.basePrice, false);
    }

    //==================== GETTERS ====================

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public VehicleType getType() {
        return type;
    }

    public BigDecimal getPricePerMinute() {
        return pricePerMinute;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public boolean isActive() {
        return active;
    }

}