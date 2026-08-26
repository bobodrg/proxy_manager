package com.proxymanager.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.proxymanager.service.ProxyHostService;
import com.proxymanager.web.dto.ProxyHostRequest;
import com.proxymanager.web.dto.ProxyHostResponse;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Admin CRUD API for ProxyHost. Not authenticated yet (Phase 3 adds JWT here).
 *
 * Note the return types: Mono/Flux instead of plain objects or ResponseEntity<T>.
 * This is the same annotation-based programming model as Spring MVC (@RestController,
 * @GetMapping, @Valid all work exactly the same), the only difference is that every
 * method describes an asynchronous pipeline instead of returning a finished result -
 * Spring WebFlux subscribes to it and writes the response once it completes.
 */
@RestController
@RequestMapping("/admin/hosts")
public class ProxyHostController {

    private final ProxyHostService service;

    public ProxyHostController(ProxyHostService service) {
        this.service = service;
    }

    @GetMapping
    public Flux<ProxyHostResponse> findAll() {
        return service.findAll().map(ProxyHostResponse::from);
    }

    @GetMapping("/{id}")
    public Mono<ProxyHostResponse> findById(@PathVariable Long id) {
        return service.findById(id).map(ProxyHostResponse::from);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProxyHostResponse> create(@Valid @RequestBody ProxyHostRequest request) {
        return service.create(request).map(ProxyHostResponse::from);
    }

    @PutMapping("/{id}")
    public Mono<ProxyHostResponse> update(@PathVariable Long id, @Valid @RequestBody ProxyHostRequest request) {
        return service.update(id, request).map(ProxyHostResponse::from);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
