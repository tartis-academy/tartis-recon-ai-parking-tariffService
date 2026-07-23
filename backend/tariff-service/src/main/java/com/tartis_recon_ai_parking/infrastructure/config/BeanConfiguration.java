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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    // Necesario: en este proyecto JacksonAutoConfiguration no registra un
    // ObjectMapper para el contexto de TariffRestAdapterTest (@SpringBootTest),
    // que lo autoinyecta para serializar los request bodies del MockMvc.
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
}