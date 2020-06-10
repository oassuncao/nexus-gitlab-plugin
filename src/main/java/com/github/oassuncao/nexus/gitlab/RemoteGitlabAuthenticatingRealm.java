package com.github.oassuncao.nexus.gitlab;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.eclipse.sisu.Description;
import org.gitlab.api.models.GitlabUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private Cache<String, GitlabPrincipal> tokenToPrincipalCache;

    public RemoteGitlabAuthenticatingRealm() {
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

        if (!enabled) {
            LOGGER.info("The Realm is disabled");
            throw new UnsupportedOperationException("The Realm is disabled");
        }

        RemoteGitlabAuthenticationToken t = (RemoteGitlabAuthenticationToken) token;
        LOGGER.info("Authenticating {}", t.getUsername());
        try {
            GitlabPrincipal principal = getPrincipal(t);
            LOGGER.info("Successfully authenticated {} with groups {}", t.getUsername(), principal.getGroups());
            return createSimpleAuthInfo(principal, token);
        } catch (Exception e) {
            LOGGER.warn("Failed authentication", e);
            return null;
        }
    }

    private GitlabPrincipal getPrincipal(RemoteGitlabAuthenticationToken token) throws GitlabAuthenticationException {
        GitlabPrincipal cached = tokenToPrincipalCache.getIfPresent(token.getUsername());
        if (cached != null) {
            LOGGER.info("Using cached principal for login: {}", token.getUsername());
            return cached;
        }

        String gitlabUsername = getGitlabUsernameByEmail(token.getUsername());

        GitlabPrincipal principal = new GitlabPrincipal();
        principal.setName(token.getName());
        principal.setUsername(token.getUsername());
        principal.setGroups(gitlabClient.getGroups(gitlabUsername));
        tokenToPrincipalCache.put(token.getUsername(), principal);
        return principal;
    }

    private String getGitlabUsernameByEmail(String email) throws GitlabAuthenticationException {
        List<GitlabUser> users = gitlabClient.findUser(email);
        for (GitlabUser user : users) {
            if (email.equals(user.getEmail()))
                return user.getUsername();
        }
        throw new GitlabAuthenticationException(String.format("User with e-mail %s not found", email));
    }

    @Override
    public void enable() {
        super.enable();
        initPrincipalCache();
    }

    private void initPrincipalCache() {
        tokenToPrincipalCache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheTtl.toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }
}
