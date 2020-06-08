package com.github.oassuncao.nexus.gitlab;

import com.github.oassuncao.nexus.gitlab.api.GitlabApiClient;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;

/**
 * @author Silvio Assunção
 * @since 1.0
 */
public class GitlabAuthenticatingRealm extends AuthorizingRealm {
// ------------------------------ FIELDS ------------------------------

    private static final String NAME = GitlabAuthenticatingRealm.class.getName();
    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabAuthenticatingRealm.class);
    boolean enabled;
    private GitlabApiClient gitlabClient;
    private String url = "https://gitlab.com";
    private String token;
    private Duration cacheTtl = Duration.parse("PT1M");//1 Minute

// --------------------- GETTER / SETTER METHODS ---------------------

    public Duration getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(Duration cacheTtl) {
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

    @Inject
    public void setGitlabClient(GitlabApiClient gitlabClient) {
        this.gitlabClient = gitlabClient;
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

        if (!enabled)
            throw new UnsupportedOperationException("The Realm is disabled");

        UsernamePasswordToken t = (UsernamePasswordToken) token;
        LOGGER.info("Authenticating {}", ((UsernamePasswordToken) token).getUsername());

        GitlabPrincipal authenticatedPrincipal;
        try {
            authenticatedPrincipal = gitlabClient.authenticate(t.getUsername(), t.getPassword());
            LOGGER.info("Successfully authenticated {}", t.getUsername());
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
        return new SimpleAuthorizationInfo(principal.getGroups());
    }

    public void enable() {
        this.gitlabClient.init(url, token, cacheTtl);
        enabled = true;
    }
}
