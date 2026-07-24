package com.tartis_recon_ai_parking.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tartis_recon_ai_parking.application.tariff.port.output.TariffPersistence;
import com.tartis_recon_ai_parking.application.tariff.usecase.ActivateTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.CreateTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.DeactivateTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.GetActiveTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.GetAllTariffsUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.GetTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.PriceCalculateUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.UpdateTariffUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


    // Necesario: en este proyecto JacksonAutoConfiguration no registra un
    // ObjectMapper para el contexto de TariffRestAdapterTest (@SpringBootTest),
    // que lo autoinyecta para serializar los request bodies del MockMvc.
/**
 * ¿QUÉ ES BEAN CONFIGURATION?
 * En una arquitectura hexagonal, los casos de uso (capa de aplicacion) no deben tener dependencias 
 * del framework (como @Service o @Component de Spring) para mantenerse puros e independientes.
 * Por ello, utilizamos una clase de configuracion (@Configuration) en la capa de infraestructura.
 * 
 * Esta clase actua como un "ensamblador": le dice a Spring como instanciar los casos de uso
 * de la capa de aplicacion y que dependencias (como puertos de persistencia) debe inyectarles,
 * registrando finalmente el resultado como un Bean en el contexto de la aplicacion.
 */
@Configuration
public class BeanConfiguration {

    // Se registra y provee el ObjectMapper como un Bean para que Spring lo use
    // al serializar y deserializar JSON, habilitando módulos automáticos (ej. para fechas).
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    public ActivateTariffUseCase activateTariffUseCase(TariffPersistence tariffPersistence) {
        return new ActivateTariffUseCase(tariffPersistence);
    }

    @Bean
    public CreateTariffUseCase createTariffUseCase(TariffPersistence tariffPersistence) {
        return new CreateTariffUseCase(tariffPersistence);
    }

    @Bean
    public DeactivateTariffUseCase deactivateTariffUseCase(TariffPersistence tariffPersistence) {
        return new DeactivateTariffUseCase(tariffPersistence);
    }

    @Bean
    public GetActiveTariffUseCase getActiveTariffUseCase(TariffPersistence tariffPersistence) {
        return new GetActiveTariffUseCase(tariffPersistence);
    }

    @Bean
    public GetAllTariffsUseCase getAllTariffsUseCase(TariffPersistence tariffPersistence) {
        return new GetAllTariffsUseCase(tariffPersistence);
    }

    @Bean
    public GetTariffUseCase getTariffUseCase(TariffPersistence tariffPersistence) {
        return new GetTariffUseCase(tariffPersistence);
    }

    @Bean
    public UpdateTariffUseCase updateTariffUseCase(TariffPersistence tariffPersistence) {
        return new UpdateTariffUseCase(tariffPersistence);
    }

    @Bean
    public PriceCalculateUseCase priceCalculateUseCase(TariffPersistence tariffPersistence) {
        return new PriceCalculateUseCase(tariffPersistence);
    }
}

