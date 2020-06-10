package com.github.oassuncao.nexus.gitlab;

import org.sonatype.nexus.security.authc.HttpHeaderAuthenticationToken;

/**
 * @author Silvio Assunção
 * @since 1.0
 */
public class RemoteGitlabAuthenticationToken extends HttpHeaderAuthenticationToken {
// ------------------------------ FIELDS ------------------------------

    private final String username;
    private final String name;

// --------------------------- CONSTRUCTORS ---------------------------

    public RemoteGitlabAuthenticationToken(String username, String name, String host) {
        super(username, username, host);
        this.username = username;
        this.name = name;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public String toString() {
        return "RemoteGitlabAuthenticationToken{" +
                "username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", host='" + getHost() + '\'' +
                '}';
    }
}
