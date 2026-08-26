package com.proxymanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.resource.PathResourceResolver;

import reactor.core.publisher.Mono;

/**
 * Serves the React build (copied into src/main/resources/static by the Docker build,
 * see the repo-root Dockerfile) and makes client-side routing work: React Router
 * handles paths like /hosts entirely in the browser, so there's no server-side file
 * at that path. Without this, a direct request (or a page refresh) on /hosts would
 * 404, since there's no hosts.html in the built assets - only index.html and the
 * fingerprinted JS/CSS.
 *
 * The custom resolver below falls back to index.html whenever the requested path
 * isn't an actual static file, letting React Router take over from there.
 *
 * This never competes with Spring Cloud Gateway (which only matches requests by the
 * Host header of a configured ProxyHost) or with /admin/** and /auth/** (annotated
 * @RestControllers are always tried before this generic resource handler) - it only
 * catches requests that neither of those handled.
 */
@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Mono<Resource> getResource(String resourcePath, Resource location) {
                        return super.getResource(resourcePath, location)
                                .switchIfEmpty(Mono.justOrEmpty(new ClassPathResource("static/index.html")));
                    }
                });
    }
}
