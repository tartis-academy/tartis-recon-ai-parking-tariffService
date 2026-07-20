package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest: Configura un entorno de pruebas enfocado únicamente en la capa JPA.
// Levanta una base de datos embebida (H2) y autoconfigura los repositorios y EntityManager.
@DataJpaTest
class TariffRepositoryTest {

    // TestEntityManager: Herramienta de Spring Boot para pruebas de persistencia que permite 
    // realizar operaciones basicas (persist, flush, etc.) en la BD de pruebas sin usar directamente 
    // el repositorio que estamos probando, aislando la fase de preparacion 
    @Autowired
    private TestEntityManager entityManager;

    // Repositorio bajo prueba
    @Autowired
    private TariffRepository tariffRepository;

    @Test
    @DisplayName("Debe retornar True si existe una tarifa con el nombre consultado")
    void shouldReturnTrueWhenNameExists() {
        // QUE HACE:
        // - Instancia y rellena un TariffEntity.
        // - Persiste la entidad directamente en la BD usando el TestEntityManager.
        // - Ejecuta la consulta existsByName con el nombre guardado.
        UUID id = UUID.randomUUID();
        TariffEntity entity = new TariffEntity();
        entity.setUniqueId(id);
        entity.setName("Standard");
        entity.setType(VehicleType.CAR);
        entity.setPricePerMinute(0.05f);
        entity.setBasePrice(2.0f);
        entity.setActive(true);

        entityManager.persistAndFlush(entity);

        boolean exists = tariffRepository.existsByName("Standard");

        // QUE DEBERIA HACER:
        // Debe retornar true indicando que el nombre ya esta registrado en el sistema.
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Debe retornar False si no existe una tarifa con el nombre consultado")
    void shouldReturnFalseWhenNameDoesNotExist() {
        // QUE HACE:
        // Llama directamente a existsByName con un nombre inexistente sin guardar nada previo.
        boolean exists = tariffRepository.existsByName("NonExistent");

        // QUE DEBERIA HACER:
        // Debe retornar false puesto que ninguna tarifa posee dicho nombre en BD.
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Debe encontrar y retornar una tarifa por su nombre si existe")
    void shouldFindTariffByNameSuccessfully() {
        // QUE HACE:
        // - Persiste una tarifa de prueba con nombre "Premium".
        // - Realiza la busqueda a traves de findByName en el repositorio.
        UUID id = UUID.randomUUID();
        TariffEntity entity = new TariffEntity();
        entity.setUniqueId(id);
        entity.setName("Premium");
        entity.setType(VehicleType.MOTORBIKE);
        entity.setPricePerMinute(0.08f);
        entity.setBasePrice(3.0f);
        entity.setActive(true);

        entityManager.persistAndFlush(entity);

        Optional<TariffEntity> result = tariffRepository.findByName("Premium");

        // QUE DEBERIA HACER:
        // Debe retornar un Optional con contenido (isPresent = true), y comprobar que 
        // los valores de la tarifa retornada correspondan con los que guardamos.
        assertThat(result).isPresent();
        assertThat(result.get().getUniqueId()).isEqualTo(id);
        assertThat(result.get().getName()).isEqualTo("Premium");
        assertThat(result.get().getType()).isEqualTo(VehicleType.MOTORBIKE);
    }

    @Test
    @DisplayName("Debe retornar un Optional vacio al buscar un nombre que no existe")
    void shouldReturnEmptyOptionalWhenNameNotFound() {
        // QUE HACE:
        // Busca una tarifa por el nombre "NonExistent" en una BD vacia.
        Optional<TariffEntity> result = tariffRepository.findByName("NonExistent");

        // QUE DEBERIA HACER:
        // Debe retornar un Optional vacio (isPresent = false / isEmpty = true).
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Debe guardar una tarifa en la base de datos y permitir recuperarla por ID")
    void shouldSaveAndLoadTariffEntity() {
        // QUE HACE:
        // - Crea un TariffEntity de prueba.
        // - Llama al metodo save del repositorio para guardarlo.
        // - Recupera la entidad utilizando findById.
        UUID id = UUID.randomUUID();
        TariffEntity entity = new TariffEntity();
        entity.setUniqueId(id);
        entity.setName("Weekend");
        entity.setType(VehicleType.CAR);
        entity.setPricePerMinute(0.06f);
        entity.setBasePrice(2.5f);
        entity.setActive(true);

        TariffEntity savedEntity = tariffRepository.save(entity);

        // QUE DEBERIA HACER:
        // La entidad guardada debe tener un ID no nulo y ser recuperable mediante findById, 
        // coincidiendo en todos sus atributos persistidos.
        assertThat(savedEntity).isNotNull();
        
        Optional<TariffEntity> loadedEntityOpt = tariffRepository.findById(id);
        assertThat(loadedEntityOpt).isPresent();
        assertThat(loadedEntityOpt.get().getName()).isEqualTo("Weekend");
        assertThat(loadedEntityOpt.get().getPricePerMinute()).isEqualTo(0.06f);
    }

    @Test
    @DisplayName("Debe encontrar tarifas por su estado activo")
    void shouldFindTariffsByActive() {
        UUID id = UUID.randomUUID();
        TariffEntity entity = new TariffEntity();
        entity.setUniqueId(id);
        entity.setName("ActiveTariff");
        entity.setType(VehicleType.CAR);
        entity.setPricePerMinute(0.05f);
        entity.setBasePrice(2.0f);
        entity.setActive(true);

        entityManager.persistAndFlush(entity);

        List<TariffEntity> activeTariffs = tariffRepository.findByActive(true);

        assertThat(activeTariffs).isNotEmpty();
        assertThat(activeTariffs.get(0).getName()).isEqualTo("ActiveTariff");
        assertThat(activeTariffs.get(0).isActive()).isTrue();
    }
}
