package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.UUID;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;

@Entity
@Table(name = "tariffs")
public class TariffEntity {

    @Id
    @Column(name = "unique_id", updatable = false, nullable = false)
    private UUID uniqueId;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    @Column(nullable = false)
    private BigDecimal pricePerMinute;

    @Column(nullable = false)
    private BigDecimal basePrice;

    @Column(nullable = false)
    private boolean active;
    @Version
    @Column(nullable = false)
    private Long version;

    // Constructor vacío requerido por JPA
    public TariffEntity() {
    }
    public TariffEntity(UUID uniqueId, String name, VehicleType type, BigDecimal pricePerMinute, BigDecimal basePrice, boolean active) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.type = type;
        this.pricePerMinute = pricePerMinute;
        this.basePrice = basePrice;
        this.active = active;
    }

    public TariffEntity(UUID uniqueId, String name, VehicleType type, BigDecimal pricePerMinute, BigDecimal basePrice, boolean active, Long version) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.type = type;
        this.pricePerMinute = pricePerMinute;
        this.basePrice = basePrice;
        this.active = active;
        this.version =version;
    }

    // Getters y Setters
    public UUID getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public BigDecimal getPricePerMinute() {
        return pricePerMinute;
    }

    public void setPricePerMinute(BigDecimal pricePerMinute) {
        this.pricePerMinute = pricePerMinute;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
     public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
