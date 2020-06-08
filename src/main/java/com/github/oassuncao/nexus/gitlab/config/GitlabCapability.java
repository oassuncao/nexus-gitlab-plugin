package com.github.oassuncao.nexus.gitlab.config;

import com.github.oassuncao.nexus.gitlab.GitlabAuthenticatingRealm;
import org.sonatype.nexus.capability.CapabilitySupport;

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

// --------------------------- CONSTRUCTORS ---------------------------

    @Inject
    public GitlabCapability(final GitlabAuthenticatingRealm authenticatingRealm) {
        this.authenticatingRealm = authenticatingRealm;
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    protected GitlabCapabilityConfiguration createConfig(Map<String, String> map) throws Exception {
        return new GitlabCapabilityConfiguration(map);
    }

    @Override
    protected void onActivate(GitlabCapabilityConfiguration config) throws Exception {
        authenticatingRealm.setCacheTtl(Duration.parse(config.getCacheTtl()));
        authenticatingRealm.setToken(config.getToken());
        authenticatingRealm.setUrl(config.getUrl());

        authenticatingRealm.enable();
        super.onActivate(config);
    }

    @Override
    protected void onPassivate(GitlabCapabilityConfiguration config) throws Exception {
        authenticatingRealm.disable();
        super.onPassivate(config);
    }
}
