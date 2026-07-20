package com.tartis_recon_ai_parking.application.tariff.port.output;

import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

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

}
