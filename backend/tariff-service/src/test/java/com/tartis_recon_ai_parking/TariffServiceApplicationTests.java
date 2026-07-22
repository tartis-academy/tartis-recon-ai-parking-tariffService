package com.tartis_recon_ai_parking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Tests de arranque de la aplicacion (TariffServiceApplication)")
class TariffServiceApplicationTests {

	@Test
	@DisplayName("Debe cargar correctamente el contexto de Spring Boot")
	void contextLoads() {
		assertThat(true).isTrue();
	}

    @Test
    @DisplayName("Debe cargar el contexto de la aplicación y ejecutar el método main de forma segura")
    void shouldLoadContextAndRunMain() {
               try (var mockedSpringApplication = org.mockito.Mockito.mockStatic(org.springframework.boot.SpringApplication.class)) {
            mockedSpringApplication.when(() -> org.springframework.boot.SpringApplication.run(TariffServiceApplication.class, new String[]{}))
                    .thenReturn(null);
            
            TariffServiceApplication.main(new String[]{});
            
            mockedSpringApplication.verify(() -> org.springframework.boot.SpringApplication.run(TariffServiceApplication.class, new String[]{}));
        }
    }

    @Test
    @DisplayName("Debe poder instanciar la clase principal")
    void shouldInstantiate() {
        TariffServiceApplication app = new TariffServiceApplication();
        org.junit.jupiter.api.Assertions.assertNotNull(app);
    }

}
