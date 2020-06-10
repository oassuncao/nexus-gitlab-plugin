package com.github.oassuncao.nexus.gitlab.api;


import com.github.oassuncao.nexus.gitlab.GitlabAuthenticationException;
import com.github.oassuncao.nexus.gitlab.GitlabPrincipal;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
    private String url;
    private String token;
    private Duration cacheTtl;

// --------------------------- CONSTRUCTORS ---------------------------

    public GitlabApiClient() {
        //no args constructor is needed
    }

// --------------------- GETTER / SETTER METHODS ---------------------


// -------------------------- OTHER METHODS --------------------------

    public GitlabPrincipal authenticate(String login, char[] token) throws GitlabAuthenticationException {
        // Combine the login and the token as the cache key since they are both used to generate the principal. If either changes we should obtain a new
        // principal.
        String cacheKey = login + "|" + new String(token);
        GitlabPrincipal cached = tokenToPrincipalCache.getIfPresent(cacheKey);
        if (cached != null) {
            LOGGER.info("Using cached principal for login: {}", login);
            return cached;
        } else {
            GitlabPrincipal principal = getPrincipal(login, token);
            tokenToPrincipalCache.put(cacheKey, principal);
            return principal;
        }
    }

    private GitlabPrincipal getPrincipal(String loginName, char[] token) throws GitlabAuthenticationException {
        GitlabUser gitlabUser;
        GitlabAPI gitlabAPI;
        try {
            gitlabAPI = GitlabAPI.connect(url, String.valueOf(token));
            gitlabUser = gitlabAPI.getUser();
        } catch (Exception e) {
            LOGGER.warn(String.format("Error on connect %s", loginName), e);
            throw new GitlabAuthenticationException(e);
        }

        if (gitlabUser == null || !loginName.equals(gitlabUser.getEmail())) {
            LOGGER.warn("Given username {} not found or does not match Gitlab E-mail!", loginName);
            throw new GitlabAuthenticationException("Given username not found or does not match Gitlab E-mail!");
        }

        GitlabPrincipal principal = new GitlabPrincipal();
        principal.setName(gitlabUser.getName());
        principal.setUsername(gitlabUser.getEmail());
        principal.setGroups(getGroups(gitlabUser.getUsername()));
        return principal;
    }

    public List<GitlabUser> findUser(String username) throws GitlabAuthenticationException {
        try {
            return client.findUsers(username);
        } catch (IOException e) {
            LOGGER.warn("Error on finding user", e);
            throw new GitlabAuthenticationException("Could not fetch users for given username");
        }
    }

    public Set<String> getGroups(String username) throws GitlabAuthenticationException {
        try {
            List<GitlabGroup> groups = client.getGroupsViaSudo(username, new Pagination().withPerPage(Pagination.MAX_ITEMS_PER_PAGE));
            return groups.stream().map(this::mapGitlabGroupToNexusRole).collect(Collectors.toSet());
        } catch (Throwable e) {
            LOGGER.warn("Error on getting groups", e);
            throw new GitlabAuthenticationException("Could not fetch groups for given username");
        }
    }

    public void init(String url, String token, Duration cacheTtl) {
        this.url = url;
        this.cacheTtl = cacheTtl;
        this.token = token;

        client = GitlabAPI.connect(url, token);
        initPrincipalCache();
    }

    private void initPrincipalCache() {
        tokenToPrincipalCache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheTtl.toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    private String mapGitlabGroupToNexusRole(GitlabGroup team) {
        return team.getPath();
    }
}
