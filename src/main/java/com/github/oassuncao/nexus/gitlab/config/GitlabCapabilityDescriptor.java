package com.github.oassuncao.nexus.gitlab.config;

import org.sonatype.goodies.i18n.I18N;
import org.sonatype.goodies.i18n.MessageBundle;
import org.sonatype.nexus.capability.CapabilityDescriptorSupport;
import org.sonatype.nexus.capability.CapabilityType;
import org.sonatype.nexus.formfields.*;

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
    private final ComboboxFormField<String> defaultRole;
    private final StringTextFormField groupAdmin;
    private final ComboboxFormField<String> roleAdmin;
    private final StringTextFormField groupPusher;
    private final ComboboxFormField<String> rolePusher;
    private final StringTextFormField headerEmail;
    private final StringTextFormField headerUsername;

// --------------------------- CONSTRUCTORS ---------------------------

    public GitlabCapabilityDescriptor() {
        setExposed(true);
        setHidden(false);

        this.url = new UrlFormField(
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

        this.defaultRole = new ComboboxFormField<String>(
                GitlabCapabilityConfiguration.DEFAULT_ROLE,
                messages.defaultRoleLabel(),
                messages.defaultRoleHelp(),
                FormField.MANDATORY
        ).withStoreApi("coreui_Role.read");

        this.groupAdmin = new StringTextFormField(
                GitlabCapabilityConfiguration.GROUP_ADMIN,
                messages.groupAdminLabel(),
                messages.groupAdminHelp(),
                FormField.MANDATORY
        );

        this.roleAdmin = new ComboboxFormField<String>(
                GitlabCapabilityConfiguration.ROLE_ADMIN,
                messages.roleAdminLabel(),
                messages.roleAdminHelp(),
                FormField.MANDATORY
        ).withStoreApi("coreui_Role.read");

        this.groupPusher = new StringTextFormField(
                GitlabCapabilityConfiguration.GROUP_PUSHER,
                messages.groupPusherLabel(),
                messages.groupPusherHelp(),
                FormField.MANDATORY
        );

        this.rolePusher = new ComboboxFormField<String>(
                GitlabCapabilityConfiguration.ROLE_PUSHER,
                messages.rolePusherLabel(),
                messages.rolePusherHelp(),
                FormField.MANDATORY
        ).withStoreApi("coreui_Role.read");

        this.headerUsername = new StringTextFormField(
                GitlabCapabilityConfiguration.HEADER_USERNAME,
                messages.headerUsernameLabel(),
                messages.headerUsernameHelp(),
                FormField.OPTIONAL
        );

        this.headerEmail = new StringTextFormField(
                GitlabCapabilityConfiguration.HEADER_EMAIL,
                messages.headerEmailLabel(),
                messages.headerEmailHelp(),
                FormField.OPTIONAL
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
        return Arrays.asList(this.url, this.token, this.cacheTtl, this.defaultRole,
                this.groupPusher, this.rolePusher, this.groupAdmin, this.roleAdmin,
                this.headerUsername, this.headerEmail);
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

        @DefaultMessage("Default Role")
        String defaultRoleLabel();

        @DefaultMessage("Default role used for any authentication user")
        String defaultRoleHelp();

        @DefaultMessage("Group Pusher")
        String groupPusherLabel();

        @DefaultMessage("Gitlab group pusher name")
        String groupPusherHelp();

        @DefaultMessage("Role Pusher")
        String rolePusherLabel();

        @DefaultMessage("Role to pusher group")
        String rolePusherHelp();

        @DefaultMessage("Group Admin")
        String groupAdminLabel();

        @DefaultMessage("Gitlab group admin name")
        String groupAdminHelp();

        @DefaultMessage("Role Admin")
        String roleAdminLabel();

        @DefaultMessage("Role to admin group")
        String roleAdminHelp();

        @DefaultMessage("Header Username")
        String headerUsernameLabel();

        @DefaultMessage("HTTP Header to identify the username")
        String headerUsernameHelp();

        @DefaultMessage("Header E-mail")
        String headerEmailLabel();

        @DefaultMessage("HTTP Header to identify the e-mail")
        String headerEmailHelp();
    }
}
