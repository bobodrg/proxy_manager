package com.proxymanager.security;

import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Wires up Spring Security for this WebFlux app. Two things make this different from
 * the Spring MVC security you might have seen elsewhere: the bean is a
 * SecurityWebFilterChain (not SecurityFilterChain), and the whole configuration is built
 * with @EnableWebFluxSecurity instead of @EnableWebSecurity.
 *
 * Only /admin/** requires authentication. Everything else - including the Gateway's
 * proxied traffic from Phase 1, which never has a path under /admin, and /auth/login
 * itself - is left open, so this phase adds authentication without touching how the
 * proxy handles traffic at all.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // CSRF protects cookie-based sessions, where the browser attaches credentials
                // automatically. A stateless Bearer-token API isn't exposed to that: the client
                // must explicitly attach the Authorization header itself, so CSRF doesn't apply.
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // We supply our own /auth/login + JWT filter instead of Spring Security's
                // built-in login mechanisms.
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/admin/**").authenticated()
                        .anyExchange().permitAll())
                // These run for requests rejected by the rule above, before they ever reach a
                // controller - GlobalExceptionHandler (a @RestControllerAdvice) only sees
                // exceptions thrown *inside* controller handling, so it can't catch these.
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((exchange, ex) -> writeJsonError(exchange, HttpStatus.UNAUTHORIZED, "Authentication required"))
                        .accessDeniedHandler((exchange, ex) -> writeJsonError(exchange, HttpStatus.FORBIDDEN, "Access denied")))
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private Mono<Void> writeJsonError(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"status\":" + status.value() + ",\"message\":\"" + message + "\"}";
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        DataBuffer buffer = bufferFactory.wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
