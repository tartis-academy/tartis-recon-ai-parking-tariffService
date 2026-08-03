package com.tartis_recon_ai_parking.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.HandlerExceptionResolver;

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
    SecurityFilterChain filterChain(HttpSecurity http,
                                    @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
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
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(bearerEntryPoint(resolver))
            )
            .exceptionHandling(eh -> eh
                // Defensivo: hoy ningun camino a nivel de filtros produce un 403
                // (todo el denegado sale por @PreAuthorize dentro del
                // DispatcherServlet, que ya cae en el resolver). Se deja como
                // seguro por si alguien anade hasRole a authorizeHttpRequests.
                .accessDeniedHandler((request, response, ex) -> resolver.resolveException(request, response, null, ex))
                .authenticationEntryPoint(bearerEntryPoint(resolver))
            );
        return http.build();
    }

    // Compone el BearerTokenAuthenticationEntryPoint por defecto (que fija el
    // status y la cabecera WWW-Authenticate, RFC 6750) con la delegacion al
    // resolver para que el cuerpo sea el ErrorResponse del adapter. Sin esto,
    // el entry point del oauth2ResourceServer no emite la cabecera y el
    // cliente no puede distinguir 401 (token caducado) de 403 (sin rol).
    private AuthenticationEntryPoint bearerEntryPoint(HandlerExceptionResolver resolver) {
        BearerTokenAuthenticationEntryPoint bearer = new BearerTokenAuthenticationEntryPoint();
        return (request, response, ex) -> {
            bearer.commence(request, response, ex);
            resolver.resolveException(request, response, null, ex);
        };
    }

    // SEC-07 (revision PR #44, actualizado 30/07 tras el fix de infra): no
    // hace falta un JwtDecoder a mano. Spring Boot soporta issuer-uri y
    // jwk-set-uri a la vez de forma nativa: usa jwk-set-uri para bajar las
    // claves (puede ser la URL interna de Docker, "keycloak:8080") y sigue
    // validando el claim iss contra issuer-uri (el externo, "localhost:8180",
    // el mismo valor que tiene fijado el consumer de Kong). Infra ya inyecta
    // las dos por variable de entorno en los 5 servicios (ver
    // docker-compose.demo.yml) - aqui basta con dejar que el autoconfigure de
    // Spring Boot las recoja, igual que en vehicle-service (verificado ahi
    // arrancando en perfil prod). El bean manual anterior usaba nombres de
    // propiedad propios que infra no rellenaba - funcionaba de casualidad,
    // pero ignoraba cualquier valor que infra intentase inyectar.

    // SEC-07: sin este converter, los roles de realm_access.roles nunca llegan a
    // convertirse en GrantedAuthority con prefijo ROLE_ (ver KeycloakRoleConverter).
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return converter;
    }
}
