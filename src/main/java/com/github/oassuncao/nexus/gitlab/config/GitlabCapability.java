package com.github.oassuncao.nexus.gitlab.config;

import com.github.oassuncao.nexus.gitlab.AbstractGitlabAuthenticationRealm;
import com.github.oassuncao.nexus.gitlab.GitlabAuthenticatingRealm;
import com.github.oassuncao.nexus.gitlab.RemoteGitlabAuthenticatingRealm;
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

    private final GitlabAuthenticatingRealm authenticatingRealm;
    private final RemoteGitlabAuthenticatingRealm remoteAuthenticatingRealm;
    private final RemoteGitlabAuthenticationTokenFactory authenticationTokenFactory;

// --------------------------- CONSTRUCTORS ---------------------------

    @Inject
    public GitlabCapability(final GitlabAuthenticatingRealm authenticatingRealm, RemoteGitlabAuthenticatingRealm remoteAuthenticatingRealm, RemoteGitlabAuthenticationTokenFactory authenticationTokenFactory) {
        this.authenticatingRealm = authenticatingRealm;
        this.remoteAuthenticatingRealm = remoteAuthenticatingRealm;
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
        enable(config, authenticatingRealm);
        enable(config, remoteAuthenticatingRealm);

        authenticationTokenFactory.setHeaderEmail(config.getHeaderEmail());
        authenticationTokenFactory.setHeaderUsername(config.getHeaderUsername());
        super.onActivate(config);
    }

    private void enable(GitlabCapabilityConfiguration config, AbstractGitlabAuthenticationRealm realm) {
        realm.setCacheTtl(Duration.parse(config.getCacheTtl()));
        realm.setToken(config.getToken());
        realm.setUrl(config.getUrl());
        realm.setDefaultRole(config.getDefaultRole());
        realm.setGroupAdmin(config.getGroupAdmin());
        realm.setRoleAdmin(config.getRoleAdmin());
        realm.setGroupPusher(config.getGroupPusher());
        realm.setRolePusher(config.getRolePusher());
        realm.enable();
    }

    @Override
    protected void onPassivate(GitlabCapabilityConfiguration config) throws Exception {
        disable(authenticatingRealm);
        disable(remoteAuthenticatingRealm);

        authenticationTokenFactory.setHeaderEmail(null);
        authenticationTokenFactory.setHeaderUsername(null);
        super.onPassivate(config);
    }

    private void disable(AbstractGitlabAuthenticationRealm realm) {
        realm.disable();
    }
}
