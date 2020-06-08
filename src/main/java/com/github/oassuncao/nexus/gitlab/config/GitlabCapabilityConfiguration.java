package com.github.oassuncao.nexus.gitlab.config;

import org.sonatype.nexus.capability.CapabilityConfigurationSupport;

import java.util.Map;

/**
 * @author Silvio Assunção
 * @since 1.0
 */
public class GitlabCapabilityConfiguration extends CapabilityConfigurationSupport {
// ------------------------------ FIELDS ------------------------------

    static final String URL = "url";
    static final String TOKEN = "token";
    static final String CACHE_TTL = "cacheTtl";

    private String url;
    private String token;
    private String cacheTtl;

// --------------------------- CONSTRUCTORS ---------------------------

    public GitlabCapabilityConfiguration(final Map<String, String> properties) {
        url = properties.get(URL);
        token = properties.get(TOKEN);
        cacheTtl = properties.get(CACHE_TTL);
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public String getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(String cacheTtl) {
        this.cacheTtl = cacheTtl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
