package com.tartis_recon_ai_parking.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.application.tariff.usecase.ActivateTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.CreateTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.DeactivateTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.GetActiveTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.GetAllTariffsUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.GetTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.UpdateTariffUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("Tests para BeanConfiguration")
class BeanConfigurationTest {

    private BeanConfiguration beanConfiguration;
    private TariffPersistence tariffPersistence;

    @BeforeEach
    void setUp() {
        beanConfiguration = new BeanConfiguration();
        tariffPersistence = mock(TariffPersistence.class);
    }

    @Test
    @DisplayName("Debe instanciar correctamente el bean ObjectMapper")
    void shouldCreateObjectMapperBean() {
        // QUE HACE: 
        // Llama al método que configura y provee el ObjectMapper.
        ObjectMapper bean = beanConfiguration.objectMapper();
        
        // QUE DEBERIA HACER: 
        // Devolver un objeto instanciado correctamente (no nulo) listo para ser usado por Spring.
        assertThat(bean).isNotNull();
    }

    @Test
    @DisplayName("Debe instanciar correctamente el bean ActivateTariffUseCase")
    void shouldCreateActivateTariffUseCaseBean() {
        // QUE HACE:
        // Llama al metodo de configuracion que provee el bean, inyectándole el mock de persistencia.
        ActivateTariffUseCase bean = beanConfiguration.activateTariffUseCase(tariffPersistence);
        
        // QUE DEBERIA HACER:
        // El bean generado no debe ser nulo, comprobando asi que la instanciacion manual es correcta.
        assertThat(bean).isNotNull();
    }

    @Test
    @DisplayName("Debe instanciar correctamente el bean CreateTariffUseCase")
    void shouldCreateCreateTariffUseCaseBean() {
        // QUE HACE:
        // Llama al metodo de configuracion que provee el bean CreateTariffUseCase.
        CreateTariffUseCase bean = beanConfiguration.createTariffUseCase(tariffPersistence);
        
        // QUE DEBERIA HACER:
        // El bean generado no debe ser nulo.
        assertThat(bean).isNotNull();
    }

    @Test
    @DisplayName("Debe instanciar correctamente el bean DeactivateTariffUseCase")
    void shouldCreateDeactivateTariffUseCaseBean() {
        // QUE HACE:
        // Llama al metodo de configuracion que provee el bean DeactivateTariffUseCase.
        DeactivateTariffUseCase bean = beanConfiguration.deactivateTariffUseCase(tariffPersistence);
        
        // QUE DEBERIA HACER:
        // El bean generado no debe ser nulo.
        assertThat(bean).isNotNull();
    }

    @Test
    @DisplayName("Debe instanciar correctamente el bean GetActiveTariffUseCase")
    void shouldCreateGetActiveTariffUseCaseBean() {
        // QUE HACE:
        // Llama al metodo de configuracion que provee el bean GetActiveTariffUseCase.
        GetActiveTariffUseCase bean = beanConfiguration.getActiveTariffUseCase(tariffPersistence);
        
        // QUE DEBERIA HACER:
        // El bean generado no debe ser nulo.
        assertThat(bean).isNotNull();
    }

    @Test
    @DisplayName("Debe instanciar correctamente el bean GetAllTariffsUseCase")
    void shouldCreateGetAllTariffsUseCaseBean() {
        // QUE HACE:
        // Llama al metodo de configuracion que provee el bean GetAllTariffsUseCase.
        GetAllTariffsUseCase bean = beanConfiguration.getAllTariffsUseCase(tariffPersistence);
        
        // QUE DEBERIA HACER:
        // El bean generado no debe ser nulo.
        assertThat(bean).isNotNull();
    }

    @Test
    @DisplayName("Debe instanciar correctamente el bean GetTariffUseCase")
    void shouldCreateGetTariffUseCaseBean() {
        // QUE HACE:
        // Llama al metodo de configuracion que provee el bean GetTariffUseCase.
        GetTariffUseCase bean = beanConfiguration.getTariffUseCase(tariffPersistence);
        
        // QUE DEBERIA HACER:
        // El bean generado no debe ser nulo.
        assertThat(bean).isNotNull();
    }

    @Test
    @DisplayName("Debe instanciar correctamente el bean UpdateTariffUseCase")
    void shouldCreateUpdateTariffUseCaseBean() {
        // QUE HACE:
        // Llama al metodo de configuracion que provee el bean UpdateTariffUseCase.
        UpdateTariffUseCase bean = beanConfiguration.updateTariffUseCase(tariffPersistence);
        
        // QUE DEBERIA HACER:
        // El bean generado no debe ser nulo.
        assertThat(bean).isNotNull();
    }
}
