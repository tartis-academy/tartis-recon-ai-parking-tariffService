package com.tartis_recon_ai_parking.application.tariff.port.output;

import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;

public interface TariffPersistence {

    // Guardar o actualizar tarifa
    Tariff save(Tariff tariff);

    // Busca una tarifa por nombre
    Optional<Tariff> findByName(String name);

    // Verifica si la tarifa existe
    boolean existsByName(String name);

    // Listamos todas las tarifas
    List<Tariff> findAll();

    // Buscamos una tarifa segun su id
    Optional<Tariff> findById(UUID id);

    // Busca tarifas por estado activo/inactivo
    List<Tariff> findByActive(boolean active);

    // Busca tarifas activas por tipo de vehiculo
    List<Tariff> findActiveByType(VehicleType type);

    // Busca tarifas activas por tipo de vehiculo aplicando bloqueo pesimista
    List<Tariff> findActiveByTypeForUpdate(VehicleType type);

}
