package com.tartis_recon_ai_parking.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
// SEC-07 (revision PR #44): sin esto, @PreAuthorize se ignora en silencio -
// ni error ni warning al arrancar, y el unico control que queda aplicandose
// es el anyRequest().authenticated() de aqui abajo. Lo activamos ya, aunque
// esta rama todavia no trae ninguna anotacion @PreAuthorize (eso es SEC-10):
// no tiene coste (no hay nada que proteger todavia) y evita que quien monte
// SEC-10 sobre esta base se encuentre con el mismo agujero silencioso.
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/actuator/health/**",
                        // SEC-07 (revision PR #44): la documentacion publicada de la
                        // API no puede quedar detras de un token - swagger-ui, el
                        // JSON de v3/api-docs y el contrato openapi.yml servido como
                        // recurso estatico tienen que ser accesibles sin autenticar.
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/openapi.yml"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    // SEC-07 (revision PR #44): no se puede usar issuer-uri a secas. Ese valor
    // dispara ademas el discovery OIDC y hace de "issuer esperado" a la vez;
    // dentro de Docker "localhost" es el propio contenedor (el discovery
    // muere) y si se cambia a "keycloak:8080" para que el discovery funcione,
    // entonces el issuer esperado deja de coincidir con el iss real del token
    // (que sigue siendo localhost:8180, el mismo valor que tiene fijado el
    // consumer de Kong) y todo da 401. Por eso van separados: jwk-set-uri es
    // solo de donde se bajan las claves (puede ser la URL interna de docker),
    // issuer es el valor que se exige en el claim iss (el externo, el real).
    @Bean
    JwtDecoder jwtDecoder(
            @Value("${security.jwt.jwk-set-uri}") String jwkSetUri,
            @Value("${security.jwt.issuer}") String expectedIssuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtIssuerValidator(expectedIssuer)));
        return decoder;
    }

    // SEC-07: sin este converter, los roles de realm_access.roles nunca llegan a
    // convertirse en GrantedAuthority con prefijo ROLE_ (ver KeycloakRoleConverter).
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return converter;
    }
}
