package com.proxymanager.security;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * Reads the "Authorization: Bearer <token>" header on every request and, if it carries
 * a valid JWT, puts an authenticated Authentication into the reactive security context
 * for that request - similar to an Express middleware that decodes a JWT and sets
 * req.user before calling next().
 *
 * If the header is missing or the token is invalid, this filter does nothing and just
 * passes the request on: no Authentication ends up in the context, so the authorization
 * rule configured in SecurityConfig (.pathMatchers("/admin/**").authenticated()) will
 * reject the request with 401 on its own - this filter never needs to reject anything
 * itself.
 */
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = header.substring("Bearer ".length());
        return jwtService.extractUsername(token)
                .map(username -> {
                    Authentication auth = new UsernamePasswordAuthenticationToken(username, null, List.of());
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                })
                .orElseGet(() -> chain.filter(exchange));
    }
}
