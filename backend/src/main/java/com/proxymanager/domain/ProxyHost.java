package com.proxymanager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "proxy_host")
public class ProxyHost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String domeniu;

    @Column(name = "target_url", nullable = false)
    private String targetUrl;

    @Column(nullable = false)
    private boolean activ;

    protected ProxyHost() {
        // no-args constructor required by JPA/Hibernate
    }

    public ProxyHost(String domeniu, String targetUrl, boolean activ) {
        this.domeniu = domeniu;
        this.targetUrl = targetUrl;
        this.activ = activ;
    }

    public Long getId() {
        return id;
    }

    public String getDomeniu() {
        return domeniu;
    }

    public void setDomeniu(String domeniu) {
        this.domeniu = domeniu;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public boolean isActiv() {
        return activ;
    }

    public void setActiv(boolean activ) {
        this.activ = activ;
    }
}
