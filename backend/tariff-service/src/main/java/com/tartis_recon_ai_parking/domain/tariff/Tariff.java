package com.tartis_recon_ai_parking.domain.tariff;

import java.util.UUID;

import com.tartis_recon_ai_parking.domain.tariff.exception.InvalidTariffException;

public class Tariff {

    private UUID uniqueId = UUID.randomUUID();

    private String name;
    private VehicleType type;
    private float pricePerMinute;
    private float basePrice;
    private boolean active;

    //Default constructor
    public Tariff(){}

    public Tariff(String name, VehicleType type, float pricePerMinute, float basePrice, boolean active) {

        //Data validation is located in the "Set" functions.
        setName(name);
        setType(type);
        setPricePerMinute(pricePerMinute);
        setBasePrice(basePrice);
        this.active = active;

    }

    //UNIQUEID getter/setter -----------------------------------
    public UUID getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    //NAME getter/setter -----------------------------------
    public String getName() {
        return name;
    }

    public void setName(String name) {
        //It will throw an exception if the variable is null or only contains blank characters.
        if (name == null || name.isBlank()) {
            throw new InvalidTariffException("Tariff name is null.");
        }
        this.name = name;
    }

    //TYPE getter/setter -----------------------------------
    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        if (type == null) throw new InvalidTariffException("Vehicle type is null.");
        this.type = type;
    }

    //PRICEPERMINUTE getter/setter -----------------------------------
    public float getPricePerMinute() {
        return pricePerMinute;
    }

    public void setPricePerMinute(float pricePerMinute) {
        if (pricePerMinute <= 0) {
            throw new InvalidTariffException("PricePerMinute must be greater than 0.");
        }
        this.pricePerMinute = pricePerMinute;
    }

    //BASEPRICE getter/setter -----------------------------------
    public float getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(float basePrice) {
        if (basePrice < 0) {
            throw new InvalidTariffException("BasePrice must be a positive number.");
        }
        this.basePrice = basePrice;
    }

    //ACTIVE getter/setter -----------------------------------
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}