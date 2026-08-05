package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; 
import java.util.UUID;
import java.util.List;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;


public interface TariffRepository extends JpaRepository<TariffEntity, UUID> {

    // Método para comprobar existencia
    boolean existsByName(String name);

    // Método para buscar por Nombre
    Optional<TariffEntity> findByName(String name);

    // Método para buscar por estado activo/inactivo
    List<TariffEntity> findByActive(boolean active);

    // Método para buscar activas por tipo
    List<TariffEntity> findByActiveTrueAndType(VehicleType type);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT t FROM TariffEntity t WHERE t.active = true AND t.type = :type")
    List<TariffEntity> findByActiveTrueAndTypeForUpdate(@org.springframework.data.repository.query.Param("type") VehicleType type);

}
