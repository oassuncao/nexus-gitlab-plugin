package com.github.oassuncao.nexus.gitlab.config;

import com.github.oassuncao.nexus.gitlab.GitlabUserManager;
import com.github.oassuncao.nexus.gitlab.filter.RemoteGitlabAuthenticationTokenFactory;
import org.sonatype.nexus.capability.CapabilitySupport;
import org.sonatype.nexus.capability.Condition;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Duration;
import java.util.Map;

/**
 * @author Silvio Assunção
 * @since 1.0
 */
@Named(GitlabCapabilityDescriptor.TYPE_ID)
public class GitlabCapability extends CapabilitySupport<GitlabCapabilityConfiguration> {
// ------------------------------ FIELDS ------------------------------

    private final GitlabUserManager userManager;
    private final RemoteGitlabAuthenticationTokenFactory authenticationTokenFactory;

// --------------------------- CONSTRUCTORS ---------------------------

    @Inject
    public GitlabCapability(GitlabUserManager userManager, RemoteGitlabAuthenticationTokenFactory authenticationTokenFactory) {
        this.userManager = userManager;
        this.authenticationTokenFactory = authenticationTokenFactory;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Capability ---------------------

    @Override
    public Condition activationCondition() {
        return conditions().capabilities().passivateCapabilityDuringUpdate();
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    protected GitlabCapabilityConfiguration createConfig(Map<String, String> map) throws Exception {
        return new GitlabCapabilityConfiguration(map);
    }

    @Override
    protected void onActivate(GitlabCapabilityConfiguration config) throws Exception {
        userManager.setCacheTtl(Duration.parse(config.getCacheTtl()));
        userManager.setToken(config.getToken());
        userManager.setUrl(config.getUrl());
        userManager.setDefaultRole(config.getDefaultRole());
        userManager.setGroupAdmin(config.getGroupAdmin());
        userManager.setRoleAdmin(config.getRoleAdmin());
        userManager.setGroupPusher(config.getGroupPusher());
        userManager.setRolePusher(config.getRolePusher());
        userManager.enable();

        authenticationTokenFactory.setHeaderEmail(config.getHeaderEmail());
        authenticationTokenFactory.setHeaderUsername(config.getHeaderUsername());
        super.onActivate(config);
    }

    @Override
    protected void onPassivate(GitlabCapabilityConfiguration config) throws Exception {
        userManager.disabled();

        authenticationTokenFactory.setHeaderEmail(null);
        authenticationTokenFactory.setHeaderUsername(null);
        super.onPassivate(config);
    }
}
