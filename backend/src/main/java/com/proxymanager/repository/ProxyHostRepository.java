package com.proxymanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proxymanager.domain.ProxyHost;

public interface ProxyHostRepository extends JpaRepository<ProxyHost, Long> {

    List<ProxyHost> findByActivTrue();
}
