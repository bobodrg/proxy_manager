package com.proxymanager.gateway;

import java.net.URI;
import java.util.List;

import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.stereotype.Component;

import com.proxymanager.domain.ProxyHost;
import com.proxymanager.repository.ProxyHostRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Dynamic route source for Spring Cloud Gateway: instead of reading routes from
 * application.yml (static, loaded once at startup), it builds them on demand from
 * the active ProxyHost rows in the database.
 *
 * The gateway calls getRouteDefinitions() whenever it needs to recompute the route
 * list (see CachingRouteLocator in Spring Cloud Gateway, which invalidates its cache
 * on a RefreshRoutesEvent and then calls this locator again). That's the entire
 * "live reload" mechanism - we just supply fresh data, Spring Cloud Gateway handles
 * the rest.
 */
@Component
public class DatabaseRouteDefinitionLocator implements RouteDefinitionLocator {

    private final ProxyHostRepository proxyHostRepository;

    public DatabaseRouteDefinitionLocator(ProxyHostRepository proxyHostRepository) {
        this.proxyHostRepository = proxyHostRepository;
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        // ProxyHostRepository is JPA => blocks the thread while it talks to Postgres over JDBC.
        // WebFlux runs on a small number of Netty threads (the event loop), which must never be
        // blocked. That's why we explicitly move the blocking call onto Schedulers.boundedElastic(),
        // a thread pool dedicated exactly to blocking code, so we don't freeze the whole server.
        return Mono.fromCallable(proxyHostRepository::findByActivTrue)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(this::toRouteDefinition);
    }

    private RouteDefinition toRouteDefinition(ProxyHost host) {
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId("proxy-host-" + host.getId());
        routeDefinition.setUri(URI.create(host.getTargetUrl()));

        // The "Host" predicate makes the route match only when the incoming request's
        // HTTP Host header is exactly the configured domain (e.g. app.example.local).
        PredicateDefinition hostPredicate = new PredicateDefinition();
        hostPredicate.setName("Host");
        // The key must be "patterns" (plural) - that's the name of the List<String> patterns
        // field in HostRoutePredicateFactory.Config; Spring automatically converts a single
        // String into a one-element list. "pattern" (without the "s") would fail silently at runtime.
        hostPredicate.addArg("patterns", host.getDomeniu());
        routeDefinition.setPredicates(List.of(hostPredicate));

        return routeDefinition;
    }
}
