package com.tartis_recon_ai_parking.infrastructure.config.tx;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.tartis_recon_ai_parking.application.tariff.usecase.ActivateTariffUseCase;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@org.junit.jupiter.api.TestInstance(org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS)
class TransactionalUseCaseTest {

    @Autowired private ActivateTariffUseCase activate;
    @Autowired private com.tartis_recon_ai_parking.application.tariff.usecase.CreateTariffUseCase create;
    @Autowired private com.tartis_recon_ai_parking.application.tariff.usecase.DeactivateTariffUseCase deactivate;
    @Autowired private com.tartis_recon_ai_parking.application.tariff.usecase.UpdateTariffUseCase update;

    java.util.stream.Stream<Object> casosDeUso() {
        return java.util.stream.Stream.of(activate, create, deactivate, update);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.MethodSource("casosDeUso")
    @DisplayName("Debe verificar que los casos de uso de escritura son proxies transaccionales")
    void todosDebenSerProxyTransaccional(Object useCase) {
        assertThat(AopUtils.isAopProxy(useCase)).isTrue();
        assertThat(AopUtils.isCglibProxy(useCase)).isTrue();
    }
}
