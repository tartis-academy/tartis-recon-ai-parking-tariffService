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

}
