package com.github.oassuncao.nexus.gitlab;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Set;

/**
 * @author Silvio Assunção
 * @since 1.0
 */
public class GitlabPrincipal implements Serializable {
// ------------------------------ FIELDS ------------------------------

    private String username;
    private String email;
    private Set<String> groups;

// --------------------- GETTER / SETTER METHODS ---------------------

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
