package com.github.oassuncao.nexus.gitlab.filter;

import com.github.oassuncao.nexus.gitlab.RemoteGitlabAuthenticationToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.security.authc.HttpHeaderAuthenticationTokenFactorySupport;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * @author Silvio Assunção
 * @since 1.0
 */
@Singleton
@Named
public class RemoteGitlabAuthenticationTokenFactory extends HttpHeaderAuthenticationTokenFactorySupport {
// ------------------------------ FIELDS ------------------------------

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteGitlabAuthenticationTokenFactory.class);
    private String headerUsername;
    private String headerEmail;

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setHeaderEmail(String headerEmail) {
        this.headerEmail = headerEmail;
    }

    public void setHeaderUsername(String headerUsername) {
        this.headerUsername = headerUsername;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface AuthenticationTokenFactory ---------------------

    @Nullable
    @Override
    public AuthenticationToken createToken(ServletRequest req, ServletResponse response) {
        HttpServletRequest request = (HttpServletRequest) req;

        if (StringUtils.isEmpty(headerUsername) && StringUtils.isEmpty(headerEmail)) {
            LOGGER.debug("The headers is not configured, the feature is disabled");
            return null;
        }

        String username = null, email = null;
        if (StringUtils.isNotEmpty(headerUsername))
            username = request.getHeader(headerUsername);

        if (StringUtils.isNotEmpty(headerEmail))
            email = request.getHeader(headerEmail);

        if (StringUtils.isEmpty(username) && StringUtils.isEmpty(email)) {
            LOGGER.debug("The headers values are empty");
            return null;
        }

        LOGGER.trace("Token with username {} e-mail {}", username, email);
        return new RemoteGitlabAuthenticationToken(username, email, req.getRemoteHost());
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    protected List<String> getHttpHeaderNames() {
        return Arrays.asList(headerUsername, headerEmail);
    }
}
