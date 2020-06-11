package com.github.oassuncao.nexus.gitlab;

import com.github.oassuncao.nexus.gitlab.api.GitlabApiClient;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Silvio Assunção
 * @since 1.0
 */
public abstract class AbstractGitlabAuthenticationRealm extends AuthorizingRealm {
// ------------------------------ FIELDS ------------------------------

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGitlabAuthenticationRealm.class);

    boolean enabled;
    protected GitlabApiClient gitlabClient;
    private String url = "https://gitlab.com";
    private String token;
    protected Duration cacheTtl = Duration.parse("PT1M");//1 Minute
    private String defaultRole;
    private String groupAdmin;
    private String roleAdmin;
    private String groupPusher;
    private String rolePusher;

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setCacheTtl(Duration cacheTtl) {
        this.cacheTtl = cacheTtl;
    }

    public void setDefaultRole(String defaultRole) {
        this.defaultRole = defaultRole;
    }

    @Inject
    public void setGitlabClient(GitlabApiClient gitlabClient) {
        this.gitlabClient = gitlabClient;
    }

    public void setGroupAdmin(String groupAdmin) {
        this.groupAdmin = groupAdmin;
    }

    public void setGroupPusher(String groupPusher) {
        this.groupPusher = groupPusher;
    }

    public void setRoleAdmin(String roleAdmin) {
        this.roleAdmin = roleAdmin;
    }

    public void setRolePusher(String rolePusher) {
        this.rolePusher = rolePusher;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUrl(String url) {
        this.url = url;
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * Creates the simple auth info.
     *
     * @param token the token
     * @return the simple authentication info
     */
    protected SimpleAuthenticationInfo createSimpleAuthInfo(GitlabPrincipal principal, AuthenticationToken token) {
        return new SimpleAuthenticationInfo(principal, token.getCredentials(), getName());
    }

    public void disable() {
        enabled = false;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        try {
            Object principal = principals.getPrimaryPrincipal();
            if (principal instanceof GitlabPrincipal) {
                GitlabPrincipal gitlabPrincipal = (GitlabPrincipal) principal;
                Set<String> roles = getRoles(gitlabPrincipal);
                LOGGER.debug("User {} received authorizations {}", gitlabPrincipal.getUsername(), roles);
                return new SimpleAuthorizationInfo(roles);
            } else {
                LOGGER.warn("Principal is not a GitlabPrincipal {} with value {}", principal.getClass().getCanonicalName(), principal.toString());
                return null;
            }
        } catch (Throwable ex) {
            LOGGER.error("Error on doGetAuthorizationInfo", ex);
            throw ex;
        }
    }

    private Set<String> getRoles(GitlabPrincipal principal) {
        Set<String> roles = new HashSet<>();
        roles.add(defaultRole);
        if (principal.getGroups().stream().anyMatch(d -> groupAdmin.equals(d)))
            roles.add(roleAdmin);
        if (principal.getGroups().stream().anyMatch(d -> groupPusher.equals(d)))
            roles.add(rolePusher);
        return roles;
    }

    public void enable() {
        this.gitlabClient.init(url, token, cacheTtl);
        enabled = true;
    }
}
