package com.github.oassuncao.nexus.gitlab;

import org.apache.commons.lang3.StringUtils;
import org.sonatype.nexus.security.authc.HttpHeaderAuthenticationToken;

/**
 * @author Silvio Assunção
 * @since 1.0
 */
public class RemoteGitlabAuthenticationToken extends HttpHeaderAuthenticationToken {
// ------------------------------ FIELDS ------------------------------

    private final String username;
    private final String email;

// --------------------------- CONSTRUCTORS ---------------------------

    public RemoteGitlabAuthenticationToken(String username, String email, String host) {
        super(username, username, host);
        this.username = username;
        this.email = email;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public String toString() {
        return "RemoteGitlabAuthenticationToken{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", host='" + getHost() + '\'' +
                '}';
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface AuthenticationToken ---------------------

    @Override
    public String getPrincipal() {
        if (StringUtils.isNotEmpty(email))
            return getEmail();
        return getUsername();
    }
}
