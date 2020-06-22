package com.github.oassuncao.nexus.gitlab;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * @author Silvio Assunção
 * @since 1.0
 */
public abstract class AbstractGitlabAuthenticationRealm extends AuthorizingRealm {
// ------------------------------ FIELDS ------------------------------

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGitlabAuthenticationRealm.class);

    protected GitlabUserManager userManager;

// --------------------- GETTER / SETTER METHODS ---------------------

    @Inject
    public void setUserManager(GitlabUserManager userManager) {
        this.userManager = userManager;
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

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        try {
            Object principal = principals.getPrimaryPrincipal();
            LOGGER.trace("Getting authorization info {} with type {}", principal, principal.getClass().getCanonicalName());
            if (principal instanceof GitlabPrincipal) {
                GitlabPrincipal gitlabPrincipal = (GitlabPrincipal) principal;
                LOGGER.debug("User {} received authorizations {}", gitlabPrincipal.getUsername(), gitlabPrincipal.getRoles());
                return new SimpleAuthorizationInfo(gitlabPrincipal.getRoles());
            } else {
                LOGGER.warn("Principal is not a GitlabPrincipal {} with value {}", principal.getClass().getCanonicalName(), principal.toString());
                return null;
            }
        } catch (Throwable ex) {
            LOGGER.error("Error on doGetAuthorizationInfo", ex);
            throw ex;
        }
    }
}
