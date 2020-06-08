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
    static final String DEFAULT_ROLE = "defaultRole";
    static final String GROUP_ADMIN = "groupAdmin";
    static final String ROLE_ADMIN = "roleAdmin";
    static final String GROUP_PUSHER = "groupPusher";
    static final String ROLE_PUSHER = "rolePusher";

    private String url;
    private String token;
    private String cacheTtl;
    private String defaultRole;
    private String groupAdmin;
    private String roleAdmin;
    private String groupPusher;
    private String rolePusher;

// --------------------------- CONSTRUCTORS ---------------------------

    public GitlabCapabilityConfiguration(final Map<String, String> properties) {
        url = properties.get(URL);
        token = properties.get(TOKEN);
        cacheTtl = properties.get(CACHE_TTL);
        defaultRole = properties.get(DEFAULT_ROLE);
        groupAdmin = properties.get(GROUP_ADMIN);
        roleAdmin = properties.get(ROLE_ADMIN);
        groupPusher = properties.get(GROUP_PUSHER);
        rolePusher = properties.get(ROLE_PUSHER);
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public String getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(String cacheTtl) {
        this.cacheTtl = cacheTtl;
    }

    public String getDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(String defaultRole) {
        this.defaultRole = defaultRole;
    }

    public String getGroupAdmin() {
        return groupAdmin;
    }

    public void setGroupAdmin(String groupAdmin) {
        this.groupAdmin = groupAdmin;
    }

    public String getGroupPusher() {
        return groupPusher;
    }

    public void setGroupPusher(String groupPusher) {
        this.groupPusher = groupPusher;
    }

    public String getRoleAdmin() {
        return roleAdmin;
    }

    public void setRoleAdmin(String roleAdmin) {
        this.roleAdmin = roleAdmin;
    }

    public String getRolePusher() {
        return rolePusher;
    }

    public void setRolePusher(String rolePusher) {
        this.rolePusher = rolePusher;
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
