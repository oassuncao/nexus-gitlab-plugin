package com.github.oassuncao.nexus.gitlab;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.eclipse.sisu.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author Silvio Assunção
 * @since 1.0
 */
@Singleton
@Named
@Description("Remote Gitlab Authentication Realm")
public class RemoteGitlabAuthenticatingRealm extends AbstractGitlabAuthenticationRealm {
// ------------------------------ FIELDS ------------------------------

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteGitlabAuthenticatingRealm.class);
    private final GitlabUserManager userManager;

// --------------------------- CONSTRUCTORS ---------------------------

    @Inject
    public RemoteGitlabAuthenticatingRealm(GitlabUserManager userManager) {
        this.userManager = userManager;
        setName("remote-gitlab");
        setCredentialsMatcher((token, info) -> true);
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Realm ---------------------

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof RemoteGitlabAuthenticationToken;
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (!(token instanceof RemoteGitlabAuthenticationToken)) {
            throw new UnsupportedTokenException(String.format("Token of type %s  is not supported. A %s is required.",
                    token.getClass().getName(), RemoteGitlabAuthenticationToken.class.getName()));
        }

        if (userManager.isDisabled()) {
            LOGGER.debug("The Realm is disabled");
            throw new UnsupportedOperationException("The Realm is disabled");
        }

        RemoteGitlabAuthenticationToken t = (RemoteGitlabAuthenticationToken) token;
        LOGGER.debug("Authenticating {}", t.getUsername());
        try {
            GitlabPrincipal principal = getPrincipal(t);
            LOGGER.debug("Successfully authenticated {} with roles {}", t.getUsername(), principal.getRoles());
            return createSimpleAuthInfo(principal, token);
        } catch (GitlabAuthenticationException e) {
            LOGGER.debug("Authentication failed", e);
        } catch (Exception e) {
            LOGGER.error("Error on authenticate", e);
        }
        return null;
    }

    private GitlabPrincipal getPrincipal(RemoteGitlabAuthenticationToken token) throws GitlabAuthenticationException {
        String username = StringUtils.isNoneEmpty(token.getUsername()) ? token.getUsername() : token.getEmail();
        return userManager.findUser(username);
    }
}
