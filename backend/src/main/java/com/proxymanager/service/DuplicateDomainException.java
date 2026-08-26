package com.proxymanager.service;

public class DuplicateDomainException extends RuntimeException {

    public DuplicateDomainException(String domeniu) {
        super("A proxy host for domain '" + domeniu + "' already exists");
    }
}
