package com.github.oassuncao.nexus.gitlab.api;


import com.github.oassuncao.nexus.gitlab.GitlabAuthenticationException;
import com.github.oassuncao.nexus.gitlab.GitlabPrincipal;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.Pagination;
import org.gitlab.api.models.GitlabGroup;
import org.gitlab.api.models.GitlabUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Silvio Assunção
 * @since 1.0
 */
@Singleton
@Named("GitlabApiClient")
public class GitlabApiClient {
// ------------------------------ FIELDS ------------------------------

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabApiClient.class);

    private GitlabAPI client;
    private Cache<String, GitlabPrincipal> usersCache;
    private String url;
    private Duration cacheTtl;

// --------------------------- CONSTRUCTORS ---------------------------

    public GitlabApiClient() {
        //no args constructor is needed
    }

// -------------------------- OTHER METHODS --------------------------

    public GitlabPrincipal authenticate(String login, char[] token) throws GitlabAuthenticationException {
        // Combine the login and the token as the cache key since they are both used to generate the principal. If either changes we should obtain a new
        // principal.
        GitlabPrincipal cached = usersCache.getIfPresent(login);
        if (cached != null) {
            LOGGER.debug("Using cached principal for login: {}", login);
            return cached;
        } else {
            GitlabPrincipal principal = getPrincipal(token);
            usersCache.put(login, principal);
            return principal;
        }
    }

    private GitlabPrincipal getPrincipal(char[] token) throws GitlabAuthenticationException {
        try {
            GitlabAPI gitlabAPI = GitlabAPI.connect(url, String.valueOf(token));
            return loadPrincipal(gitlabAPI.getUser());
        } catch (Exception e) {
            throw new GitlabAuthenticationException("Error on validating user and token", e);
        }
    }

    private GitlabPrincipal loadPrincipal(GitlabUser user) throws GitlabAuthenticationException {
        if (user == null)
            throw new GitlabAuthenticationException("User not found");

        return GitlabPrincipal.mapFrom(user, getGroups(user));
    }

    public List<GitlabGroup> getGroups(GitlabUser user) throws GitlabAuthenticationException {
        try {
            return client.getGroupsViaSudo(user.getUsername(), new Pagination().withPerPage(Pagination.MAX_ITEMS_PER_PAGE));
        } catch (Throwable e) {
            throw new GitlabAuthenticationException("Could not fetch groups for given username", e);
        }
    }

    public GitlabPrincipal findUser(String username) throws GitlabAuthenticationException {
        try {
            GitlabPrincipal cache = usersCache.getIfPresent(username);
            if (cache != null)
                return cache;

            List<GitlabUser> users = client.findUsers(username);
            if (CollectionUtils.isEmpty(users))
                throw new GitlabAuthenticationException("User not found");

            if (users.size() > 1)
                throw new GitlabAuthenticationException(String.format("The username / e-mail %s contains more than one user", username));

            GitlabUser user = users.get(0);
            GitlabPrincipal principal = loadPrincipal(user);
            usersCache.put(username, principal);
            return principal;
        } catch (IOException e) {
            throw new GitlabAuthenticationException("Could not fetch users for given username", e);
        }
    }

    public void init(String url, String token, Duration cacheTtl) {
        this.url = url;
        this.cacheTtl = cacheTtl;

        client = GitlabAPI.connect(url, token);
        initUsersCache();
    }

    private void initUsersCache() {
        usersCache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheTtl.toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }
}
