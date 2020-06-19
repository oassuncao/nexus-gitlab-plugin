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
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private Cache<String, GitlabPrincipal> tokenToPrincipalCache;
    private Cache<String, List<GitlabUser>> usersCache;
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
        String cacheKey = login + "|" + new String(token);
        GitlabPrincipal cached = tokenToPrincipalCache.getIfPresent(cacheKey);
        if (cached != null) {
            LOGGER.debug("Using cached principal for login: {}", login);
            return cached;
        } else {
            GitlabPrincipal principal = getPrincipal(token);
            tokenToPrincipalCache.put(cacheKey, principal);
            return principal;
        }
    }

    private GitlabPrincipal getPrincipal(char[] token) throws GitlabAuthenticationException {
        GitlabUser gitlabUser;
        GitlabAPI gitlabAPI;
        try {
            gitlabAPI = GitlabAPI.connect(url, String.valueOf(token));
            gitlabUser = gitlabAPI.getUser();
        } catch (Exception e) {
            throw new GitlabAuthenticationException("Error on validating user and token", e);
        }

        GitlabPrincipal principal = new GitlabPrincipal();
        principal.setEmail(gitlabUser.getEmail());
        principal.setUsername(gitlabUser.getUsername());
        principal.setGroups(getGroups(gitlabUser.getUsername()));
        return principal;
    }

    public Set<String> getGroups(String username) throws GitlabAuthenticationException {
        try {
            List<GitlabGroup> groups = client.getGroupsViaSudo(username, new Pagination().withPerPage(Pagination.MAX_ITEMS_PER_PAGE));
            return groups.stream().map(this::mapGitlabGroupToNexusRole).collect(Collectors.toSet());
        } catch (Throwable e) {
            throw new GitlabAuthenticationException("Could not fetch groups for given username", e);
        }
    }

    public List<GitlabUser> findUser(String username) throws GitlabAuthenticationException {
        try {
            List<GitlabUser> cache = usersCache.getIfPresent(username);
            if (cache != null)
                return cache;

            List<GitlabUser> users = client.findUsers(username);
            if (CollectionUtils.isNotEmpty(users))
                usersCache.put(username, users);
            return users;
        } catch (IOException e) {
            throw new GitlabAuthenticationException("Could not fetch users for given username", e);
        }
    }

    public void init(String url, String token, Duration cacheTtl) {
        this.url = url;
        this.cacheTtl = cacheTtl;

        client = GitlabAPI.connect(url, token);
        initPrincipalCache();
        initUsersCache();
    }

    private void initPrincipalCache() {
        tokenToPrincipalCache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheTtl.toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    private void initUsersCache() {
        usersCache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheTtl.toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    private String mapGitlabGroupToNexusRole(GitlabGroup team) {
        return team.getPath();
    }
}
