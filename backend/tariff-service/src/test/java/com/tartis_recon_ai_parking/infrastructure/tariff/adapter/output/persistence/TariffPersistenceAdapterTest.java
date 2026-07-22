package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;


import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TariffPersistenceAdapterTest {

    @Mock
    private TariffRepository tariffRepository;

    @Mock
    private TariffPersistenceMapper tariffPersistenceMapper;

    @InjectMocks
    private TariffPersistenceAdapter tariffPersistenceAdapter;

    @Test
    @DisplayName("Debe guardar una tarifa mapeandola a entidad y retornandola convertida a dominio de nuevo")
    void shouldSaveTariffSuccessfully() {
        UUID id = UUID.randomUUID();
        Tariff tariff = Tariff.reconstruct(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        TariffEntity entity = new TariffEntity();
        entity.setUniqueId(id);
        entity.setName("Standard");

        when(tariffPersistenceMapper.toEntity(tariff)).thenReturn(entity);
        when(tariffRepository.save(entity)).thenReturn(entity);
        when(tariffPersistenceMapper.toDomain(entity)).thenReturn(tariff);

        Tariff result = tariffPersistenceAdapter.save(tariff);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Standard");
        verify(tariffPersistenceMapper, times(1)).toEntity(tariff);
        verify(tariffRepository, times(1)).save(entity);
        verify(tariffPersistenceMapper, times(1)).toDomain(entity);
    }

    @Test
    @DisplayName("Debe retornar un Optional con la tarifa si el nombre consultado existe en BD")
    void shouldFindTariffByNameSuccessfully() {
        UUID id = UUID.randomUUID();
        TariffEntity entity = new TariffEntity();
        entity.setUniqueId(id);
        entity.setName("Standard");
        
        Tariff tariff = Tariff.reconstruct(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(tariffRepository.findByName("Standard")).thenReturn(Optional.of(entity));
        when(tariffPersistenceMapper.toDomain(entity)).thenReturn(tariff);

        Optional<Tariff> result = tariffPersistenceAdapter.findByName("Standard");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Standard");
        verify(tariffRepository, times(1)).findByName("Standard");
        verify(tariffPersistenceMapper, times(1)).toDomain(entity);
    }

    @Test
    @DisplayName("Debe retornar un Optional vacio al buscar por nombre si no existe en BD")
    void shouldReturnEmptyOptionalWhenNameNotFound() {

        when(tariffRepository.findByName("Premium")).thenReturn(Optional.empty());

        Optional<Tariff> result = tariffPersistenceAdapter.findByName("Premium");

        assertThat(result).isEmpty();
        verify(tariffRepository, times(1)).findByName("Premium");
        verify(tariffPersistenceMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Debe retornar un Optional con la tarifa si el ID existe en BD")
    void shouldFindTariffByIdSuccessfully() {

        UUID id = UUID.randomUUID();
        TariffEntity entity = new TariffEntity();
        entity.setUniqueId(id);

        Tariff tariff = Tariff.reconstruct(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(tariffRepository.findById(id)).thenReturn(Optional.of(entity));
        when(tariffPersistenceMapper.toDomain(entity)).thenReturn(tariff);

        Optional<Tariff> result = tariffPersistenceAdapter.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getUniqueId()).isEqualTo(id);
        verify(tariffRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe retornar true si el nombre ya esta registrado en BD")
    void shouldReturnTrueWhenNameExists() {
        when(tariffRepository.existsByName("Standard")).thenReturn(true);

        boolean exists = tariffPersistenceAdapter.existsByName("Standard");

        assertThat(exists).isTrue();
        verify(tariffRepository, times(1)).existsByName("Standard");
    }

    @Test
    @DisplayName("Debe retornar la lista completa de tarifas convertida a objetos de dominio")
    void shouldFindAllTariffs() {
        TariffEntity entity1 = new TariffEntity();
        entity1.setName("Standard");
        TariffEntity entity2 = new TariffEntity();
        entity2.setName("Premium");

        Tariff domain1 = Tariff.reconstruct(UUID.randomUUID(), "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);
        Tariff domain2 = Tariff.reconstruct(UUID.randomUUID(), "Premium", VehicleType.MOTORBIKE, new BigDecimal("0.08"), new BigDecimal("3.0"), true);

        when(tariffRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(tariffPersistenceMapper.toDomain(entity1)).thenReturn(domain1);
        when(tariffPersistenceMapper.toDomain(entity2)).thenReturn(domain2);

        List<Tariff> result = tariffPersistenceAdapter.findAll();

        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Standard");
        assertThat(result.get(1).getName()).isEqualTo("Premium");
        verify(tariffRepository, times(1)).findAll();
        verify(tariffPersistenceMapper, times(2)).toDomain(any());
    }

    @Test
    @DisplayName("Debe retornar la lista de tarifas filtradas por estado activo")
    void shouldFindTariffsByActive() {
        TariffEntity entity1 = new TariffEntity();
        entity1.setName("Standard");

        Tariff domain1 = Tariff.reconstruct(UUID.randomUUID(), "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(tariffRepository.findByActive(true)).thenReturn(List.of(entity1));
        when(tariffPersistenceMapper.toDomain(entity1)).thenReturn(domain1);

        List<Tariff> result = tariffPersistenceAdapter.findByActive(true);

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Standard");
        verify(tariffRepository, times(1)).findByActive(true);
        verify(tariffPersistenceMapper, times(1)).toDomain(any());
    }

    @Test
    @DisplayName("Debe retornar la lista de tarifas activas filtradas por tipo de vehiculo")
    void shouldFindActiveTariffsByType() {
        TariffEntity entity1 = new TariffEntity();
        entity1.setName("Standard");

        Tariff domain1 = Tariff.reconstruct(UUID.randomUUID(), "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(tariffRepository.findByActiveTrueAndType(VehicleType.CAR)).thenReturn(List.of(entity1));
        when(tariffPersistenceMapper.toDomain(entity1)).thenReturn(domain1);

        List<Tariff> result = tariffPersistenceAdapter.findActiveByType(VehicleType.CAR);

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Standard");
        verify(tariffRepository, times(1)).findByActiveTrueAndType(VehicleType.CAR);
        verify(tariffPersistenceMapper, times(1)).toDomain(any());
    }
}
