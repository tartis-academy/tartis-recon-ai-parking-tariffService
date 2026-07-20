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

// @ExtendWith(MockitoExtension.class): Habilita el soporte de Mockito en JUnit para pruebas unitarias rapidas.
@ExtendWith(MockitoExtension.class)
class TariffPersistenceAdapterTest {

    // @Mock: Genera mocks de las dependencias que requiere el adaptador.
    @Mock
    private TariffRepository tariffRepository;

    @Mock
    private TariffPersistenceMapper tariffPersistenceMapper;

    // @InjectMocks: Crea la instancia de la clase bajo prueba e inyecta automaticamente los mocks anteriores.
    @InjectMocks
    private TariffPersistenceAdapter tariffPersistenceAdapter;

    @Test
    @DisplayName("Debe guardar una tarifa mapeandola a entidad y retornandola convertida a dominio de nuevo")
    void shouldSaveTariffSuccessfully() {
        // QUE HACE:
        // - Instancia una tarifa de dominio.
        // - Simula una entidad de persistencia y la tarifa de retorno mapeada.
        // - Configura los mocks del mapper y del repositorio.
        // - Ejecuta el metodo save del adaptador.
        UUID id = UUID.randomUUID();
        Tariff tariff = Tariff.reconstruct(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        TariffEntity entity = new TariffEntity();
        entity.setUniqueId(id);
        entity.setName("Standard");

        when(tariffPersistenceMapper.toEntity(tariff)).thenReturn(entity);
        when(tariffRepository.save(entity)).thenReturn(entity);
        when(tariffPersistenceMapper.toDomain(entity)).thenReturn(tariff);

        Tariff result = tariffPersistenceAdapter.save(tariff);

        // QUE DEBERIA HACER:
        // Debe retornar la tarifa persistida correctamente mapeada de vuelta y verificar que se
        // llamo exactamente una vez a los metodos del mapper y del repositorio.
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Standard");
        verify(tariffPersistenceMapper, times(1)).toEntity(tariff);
        verify(tariffRepository, times(1)).save(entity);
        verify(tariffPersistenceMapper, times(1)).toDomain(entity);
    }

    @Test
    @DisplayName("Debe retornar un Optional con la tarifa si el nombre consultado existe en BD")
    void shouldFindTariffByNameSuccessfully() {
        // QUE HACE:
        // - Simula la respuesta del repositorio conteniendo una entidad.
        // - Configura el mapper para que traduzca dicha entidad al dominio.
        // - Ejecuta la busqueda findByName.
        UUID id = UUID.randomUUID();
        TariffEntity entity = new TariffEntity();
        entity.setUniqueId(id);
        entity.setName("Standard");
        
        Tariff tariff = Tariff.reconstruct(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(tariffRepository.findByName("Standard")).thenReturn(Optional.of(entity));
        when(tariffPersistenceMapper.toDomain(entity)).thenReturn(tariff);

        Optional<Tariff> result = tariffPersistenceAdapter.findByName("Standard");

        // QUE DEBERIA HACER:
        // Debe retornar un Optional con el objeto de dominio y verificar la interaccion de los mocks.
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Standard");
        verify(tariffRepository, times(1)).findByName("Standard");
        verify(tariffPersistenceMapper, times(1)).toDomain(entity);
    }

    @Test
    @DisplayName("Debe retornar un Optional vacio al buscar por nombre si no existe en BD")
    void shouldReturnEmptyOptionalWhenNameNotFound() {
        // QUE HACE:
        // - Configura el mock del repositorio para retornar un Optional vacio.
        // - Invoca findByName.
        when(tariffRepository.findByName("Premium")).thenReturn(Optional.empty());

        Optional<Tariff> result = tariffPersistenceAdapter.findByName("Premium");

        // QUE DEBERIA HACER:
        // Debe retornar un Optional vacio y asegurar que nunca se llamo al mapper.
        assertThat(result).isEmpty();
        verify(tariffRepository, times(1)).findByName("Premium");
        verify(tariffPersistenceMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Debe retornar un Optional con la tarifa si el ID existe en BD")
    void shouldFindTariffByIdSuccessfully() {
        // QUE HACE:
        // - Genera un ID aleatorio.
        // - Simula que el repositorio encuentra la entidad con ese ID.
        // - Configura el mapper.
        // - Ejecuta la busqueda findById.
        UUID id = UUID.randomUUID();
        TariffEntity entity = new TariffEntity();
        entity.setUniqueId(id);

        Tariff tariff = Tariff.reconstruct(id, "Standard", VehicleType.CAR, new BigDecimal("0.05"), new BigDecimal("2.0"), true);

        when(tariffRepository.findById(id)).thenReturn(Optional.of(entity));
        when(tariffPersistenceMapper.toDomain(entity)).thenReturn(tariff);

        Optional<Tariff> result = tariffPersistenceAdapter.findById(id);

        // QUE DEBERIA HACER:
        // Debe retornar un Optional con la tarifa de dominio correspondiente.
        assertThat(result).isPresent();
        assertThat(result.get().getUniqueId()).isEqualTo(id);
        verify(tariffRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe retornar true si el nombre ya esta registrado en BD")
    void shouldReturnTrueWhenNameExists() {
        // QUE HACE:
        // - Configura el repositorio para indicar que el nombre si existe (true).
        // - Invoca existsByName en el adaptador.
        when(tariffRepository.existsByName("Standard")).thenReturn(true);

        boolean exists = tariffPersistenceAdapter.existsByName("Standard");

        // QUE DEBERIA HACER:
        // Debe retornar true.
        assertThat(exists).isTrue();
        verify(tariffRepository, times(1)).existsByName("Standard");
    }

    @Test
    @DisplayName("Debe retornar la lista completa de tarifas convertida a objetos de dominio")
    void shouldFindAllTariffs() {
        // QUE HACE:
        // - Prepara una lista de entidades en base de datos.
        // - Configura los mocks para retornar las entidades y mapear cada una de ellas a dominio.
        // - Llama al metodo findAll.
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

        // QUE DEBERIA HACER:
        // Debe retornar una lista de tamaño 2 y verificar que cada elemento ha sido correctamente
        // traducido al dominio.
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
}
