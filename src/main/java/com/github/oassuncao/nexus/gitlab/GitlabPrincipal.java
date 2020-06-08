package com.github.oassuncao.nexus.gitlab;

import java.io.Serializable;
import java.util.Set;

/**
 * @author Silvio Assunção
 * @since 1.0
 */
public class GitlabPrincipal implements Serializable {
// ------------------------------ FIELDS ------------------------------

    private String username;
    private String name;
    private Set<String> groups;

// --------------------- GETTER / SETTER METHODS ---------------------

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public String toString() {
        return username;
    }
}
