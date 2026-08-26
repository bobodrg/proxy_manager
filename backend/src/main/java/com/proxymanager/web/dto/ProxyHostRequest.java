package com.proxymanager.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for POST/PUT /admin/hosts. A record: Java generates the constructor,
 * accessors (domeniu(), targetUrl(), activ()), equals/hashCode/toString for us -
 * a good fit for an immutable data holder like this.
 */
public record ProxyHostRequest(

        @NotBlank(message = "domeniu is required")
        String domeniu,

        @NotBlank(message = "targetUrl is required")
        @Pattern(regexp = "^https?://.+", message = "targetUrl must start with http:// or https://")
        String targetUrl,

        @NotNull(message = "activ is required")
        Boolean activ
) {
}
