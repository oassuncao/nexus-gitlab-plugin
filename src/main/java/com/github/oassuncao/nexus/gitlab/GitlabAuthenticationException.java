package com.github.oassuncao.nexus.gitlab;

/**
 * @author Silvio Assunção
 * @since 1.0
 */
public class GitlabAuthenticationException extends Exception {
// --------------------------- CONSTRUCTORS ---------------------------

    public GitlabAuthenticationException(String message) {
        super(message);
    }

    public GitlabAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
