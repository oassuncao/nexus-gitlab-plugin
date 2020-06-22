package com.github.oassuncao.nexus.gitlab;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.gitlab.api.models.GitlabGroup;
import org.gitlab.api.models.GitlabUser;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Silvio Assunção
 * @since 1.0
 */
public class GitlabPrincipal implements Serializable {
// ------------------------------ FIELDS ------------------------------

    private static final long serialVersionUID = -678547881708313150L;
    private String username;
    private String name;
    private String email;
    private Boolean blocked;
    private Set<String> groups;
    private Set<String> roles;

// -------------------------- STATIC METHODS --------------------------

    public static GitlabPrincipal mapFrom(GitlabUser user, List<GitlabGroup> groups) {
        GitlabPrincipal principal = new GitlabPrincipal();
        principal.setEmail(user.getEmail());
        principal.setName(user.getName());
        principal.setBlocked(user.isBlocked());
        principal.setUsername(user.getUsername());
        if (CollectionUtils.isNotEmpty(groups))
            principal.setGroups(groups.stream().map(GitlabGroup::getPath).collect(Collectors.toSet()));
        return principal;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public String toString() {
        if (StringUtils.isNotEmpty(username))
            return username;
        return email;
    }
}
