package com.github.oassuncao.nexus.gitlab.config;

import org.sonatype.goodies.i18n.I18N;
import org.sonatype.goodies.i18n.MessageBundle;
import org.sonatype.nexus.capability.CapabilityDescriptorSupport;
import org.sonatype.nexus.capability.CapabilityType;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.PasswordFormField;
import org.sonatype.nexus.formfields.StringTextFormField;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

import static org.sonatype.nexus.capability.CapabilityType.capabilityType;

/**
 * @author Silvio Assunção
 * @since 1.0
 */
@Named(GitlabCapabilityDescriptor.TYPE_ID)
@Singleton
public class GitlabCapabilityDescriptor extends CapabilityDescriptorSupport<GitlabCapabilityConfiguration> {
// ------------------------------ FIELDS ------------------------------

    public static final String TYPE_ID = "gitlabRealm";
    public static final CapabilityType TYPE = capabilityType(TYPE_ID);
    private static final Messages messages = I18N.create(Messages.class);

    private final StringTextFormField url;
    private final PasswordFormField token;
    private final StringTextFormField cacheTtl;

// --------------------------- CONSTRUCTORS ---------------------------

    public GitlabCapabilityDescriptor() {
        setExposed(true);
        setHidden(false);

        this.url = new StringTextFormField(
                GitlabCapabilityConfiguration.URL,
                messages.urlLabel(),
                messages.urlHelp(),
                FormField.MANDATORY
        );

        this.token = new PasswordFormField(
                GitlabCapabilityConfiguration.TOKEN,
                messages.tokenLabel(),
                messages.tokenHelp(),
                FormField.MANDATORY
        );

        this.cacheTtl = new StringTextFormField(
                GitlabCapabilityConfiguration.CACHE_TTL,
                messages.cacheTtlLabel(),
                messages.cacheTtlHelp(),
                FormField.MANDATORY
        );
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface CapabilityDescriptor ---------------------

    @Override
    public CapabilityType type() {
        return TYPE;
    }

    @Override
    public String name() {
        return messages.name();
    }

    @Override
    public List<FormField> formFields() {
        return Arrays.asList(this.url, this.token, this.cacheTtl);
    }

    @Override
    public String about() {
        return messages.description();
    }

// -------------------------- INNER CLASSES --------------------------

    private interface Messages extends MessageBundle {
        @DefaultMessage("Gitlab Realm")
        String name();

        @DefaultMessage("Gitlab Realm Authentication configuration")
        String description();

        @DefaultMessage("Gitlab Url")
        String urlLabel();

        @DefaultMessage("Gitlab URL (Ex: https://gitlab.com)")
        String urlHelp();

        @DefaultMessage("Token")
        String tokenLabel();

        @DefaultMessage("Gitlab Token Authentication")
        String tokenHelp();

        @DefaultMessage("Cache TTL")
        String cacheTtlLabel();

        @DefaultMessage("Duration cache of the authentication (Ex: PT1M)")
        String cacheTtlHelp();
    }
}
