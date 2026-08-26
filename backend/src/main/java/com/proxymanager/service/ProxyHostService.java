package com.proxymanager.service;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.proxymanager.domain.ProxyHost;
import com.proxymanager.repository.ProxyHostRepository;
import com.proxymanager.web.dto.ProxyHostRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Business logic for managing ProxyHost records. Every method that changes data
 * (create/update/delete) publishes a RefreshRoutesEvent afterwards - the same event
 * Spring Cloud Gateway's built-in CachingRouteLocator already listens for (see
 * DatabaseRouteDefinitionLocator from Phase 1). This is what makes route changes take
 * effect immediately, replacing the manual POST /actuator/gateway/refresh call we used
 * for testing in Phase 1.
 *
 * As in Phase 1, every JPA call (blocking) is wrapped in Mono.fromCallable(...)
 * .subscribeOn(Schedulers.boundedElastic()) so it never blocks a Netty event-loop thread.
 */
@Service
public class ProxyHostService {

    private final ProxyHostRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public ProxyHostService(ProxyHostRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    public Flux<ProxyHost> findAll() {
        return Mono.fromCallable(repository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<ProxyHost> findById(Long id) {
        return Mono.fromCallable(() -> repository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty)
                .switchIfEmpty(Mono.error(new ProxyHostNotFoundException(id)));
    }

    public Mono<ProxyHost> create(ProxyHostRequest request) {
        return Mono.fromCallable(() -> {
                    if (repository.existsByDomeniu(request.domeniu())) {
                        throw new DuplicateDomainException(request.domeniu());
                    }
                    ProxyHost host = new ProxyHost(request.domeniu(), request.targetUrl(), request.activ());
                    return repository.save(host);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(saved -> publishRefresh());
    }

    public Mono<ProxyHost> update(Long id, ProxyHostRequest request) {
        return Mono.fromCallable(() -> {
                    ProxyHost host = repository.findById(id)
                            .orElseThrow(() -> new ProxyHostNotFoundException(id));
                    if (repository.existsByDomeniuAndIdNot(request.domeniu(), id)) {
                        throw new DuplicateDomainException(request.domeniu());
                    }
                    host.setDomeniu(request.domeniu());
                    host.setTargetUrl(request.targetUrl());
                    host.setActiv(request.activ());
                    return repository.save(host);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(saved -> publishRefresh());
    }

    public Mono<Void> delete(Long id) {
        return Mono.fromRunnable(() -> {
                    if (!repository.existsById(id)) {
                        throw new ProxyHostNotFoundException(id);
                    }
                    repository.deleteById(id);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(ignored -> publishRefresh())
                .then();
    }

    private void publishRefresh() {
        eventPublisher.publishEvent(new RefreshRoutesEvent(this));
    }
}
