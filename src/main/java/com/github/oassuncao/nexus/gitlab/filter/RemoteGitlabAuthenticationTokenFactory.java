package com.github.oassuncao.nexus.gitlab.filter;

import com.github.oassuncao.nexus.gitlab.RemoteGitlabAuthenticationToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationToken;
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

    private String headerUsername;
    private String headerName;

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
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
        if (StringUtils.isEmpty(headerUsername) || StringUtils.isEmpty(request.getHeader(headerUsername))) {
            return null;
        }

        String userName = request.getHeader(headerUsername);
        String name = null;
        if (StringUtils.isNotEmpty(headerName))
            name = request.getHeader(headerName);

        return new RemoteGitlabAuthenticationToken(userName, name, req.getRemoteHost());
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    protected List<String> getHttpHeaderNames() {
        return Arrays.asList(headerUsername, headerName);
    }
}
