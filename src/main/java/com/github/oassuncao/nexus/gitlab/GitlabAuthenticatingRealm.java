package com.github.oassuncao.nexus.gitlab;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.eclipse.sisu.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author Silvio Assunção
 * @since 1.0
 */
@Singleton
@Named
@Description("Gitlab Authentication Realm")
public class GitlabAuthenticatingRealm extends AbstractGitlabAuthenticationRealm {
// ------------------------------ FIELDS ------------------------------

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabAuthenticatingRealm.class);

// ------------------------ INTERFACE METHODS ------------------------

    public GitlabAuthenticatingRealm() {
        setName("gitlab");
    }


// --------------------- Interface Realm ---------------------

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof UsernamePasswordToken;
    }

// -------------------------- OTHER METHODS --------------------------

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
            return createSimpleAuthInfo(authenticatedPrincipal, t);
        } catch (Exception e) {
            LOGGER.warn("Failed authentication", e);
            return null;
        }
    }
}
