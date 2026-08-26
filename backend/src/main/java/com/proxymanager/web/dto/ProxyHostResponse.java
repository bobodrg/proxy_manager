package com.proxymanager.web.dto;

import com.proxymanager.domain.ProxyHost;

/**
 * Response body for /admin/hosts. Kept separate from the ProxyHost entity so the
 * JPA entity never gets serialized directly to JSON (and so the API shape can evolve
 * independently of the persistence model).
 */
public record ProxyHostResponse(Long id, String domeniu, String targetUrl, boolean activ) {

    public static ProxyHostResponse from(ProxyHost host) {
        return new ProxyHostResponse(host.getId(), host.getDomeniu(), host.getTargetUrl(), host.isActiv());
    }
}
