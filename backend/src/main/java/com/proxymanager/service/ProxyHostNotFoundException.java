package com.proxymanager.service;

public class ProxyHostNotFoundException extends RuntimeException {

    public ProxyHostNotFoundException(Long id) {
        super("Proxy host not found: " + id);
    }
}
