package com.github.oassuncao.nexus.gitlab;

import com.github.oassuncao.nexus.gitlab.api.GitlabApiClient;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.eclipse.sisu.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Silvio Assunção
 * @since 1.0
 */
@Singleton
@Named
@Description("Gitlab Authentication Realm")
public class GitlabAuthenticatingRealm extends AuthorizingRealm {
// ------------------------------ FIELDS ------------------------------

    private static final String NAME = GitlabAuthenticatingRealm.class.getName();
    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabAuthenticatingRealm.class);
    boolean enabled;
    private GitlabApiClient gitlabClient;
    private String url = "https://gitlab.com";
    private String token;
    private Duration cacheTtl = Duration.parse("PT1M");//1 Minute
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

    public void disable() {
        enabled = false;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (!(token instanceof UsernamePasswordToken)) {
            throw new UnsupportedTokenException(String.format("Token of type %s  is not supported. A %s is required.",
                    token.getClass().getName(), UsernamePasswordToken.class.getName()));
        }

        if (!enabled) {
            LOGGER.info("The Realm is disabled");
            throw new UnsupportedOperationException("The Realm is disabled");
        }

        UsernamePasswordToken t = (UsernamePasswordToken) token;
        LOGGER.info("Authenticating {}", ((UsernamePasswordToken) token).getUsername());
        GitlabPrincipal authenticatedPrincipal;
        try {
            authenticatedPrincipal = gitlabClient.authenticate(t.getUsername(), t.getPassword());
            LOGGER.info("Successfully authenticated {} with groups {}", t.getUsername(), authenticatedPrincipal.getGroups());
        } catch (Exception e) {
            LOGGER.warn("Failed authentication", e);
            return null;
        }

        return createSimpleAuthInfo(authenticatedPrincipal, t);
    }

    /**
     * Creates the simple auth info.
     *
     * @param token the token
     * @return the simple authentication info
     */
    private SimpleAuthenticationInfo createSimpleAuthInfo(GitlabPrincipal principal, UsernamePasswordToken token) {
        return new SimpleAuthenticationInfo(principal, token.getCredentials(), NAME);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        GitlabPrincipal principal = (GitlabPrincipal) principals.getPrimaryPrincipal();
        Set<String> roles = getRoles(principal);
        LOGGER.info("User {} received authorizations {}", principal.getUsername(), roles);
        return new SimpleAuthorizationInfo(roles);
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
