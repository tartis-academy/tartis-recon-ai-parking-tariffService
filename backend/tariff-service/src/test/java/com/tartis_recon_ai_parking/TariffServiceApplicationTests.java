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
		// Al igual que en spot-service, comprueba que se levanta sin lanzar excepciones (por problemas de dependencias, beans o configuracion)
		assertThat(true).isTrue();
	}

}
