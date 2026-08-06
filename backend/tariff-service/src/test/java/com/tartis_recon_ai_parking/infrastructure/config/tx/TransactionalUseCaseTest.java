package com.tartis_recon_ai_parking.infrastructure.config.tx;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.tartis_recon_ai_parking.application.tariff.usecase.ActivateTariffUseCase;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TransactionalUseCaseTest {

    @Autowired
    private ActivateTariffUseCase activateTariffUseCase;

    @Test
    @DisplayName("Debe verificar que ActivateTariffUseCase es un proxy transaccional")
    void shouldBeAopProxy() {
        assertThat(AopUtils.isAopProxy(activateTariffUseCase)).isTrue();
        assertThat(AopUtils.isCglibProxy(activateTariffUseCase)).isTrue();
    }
}
