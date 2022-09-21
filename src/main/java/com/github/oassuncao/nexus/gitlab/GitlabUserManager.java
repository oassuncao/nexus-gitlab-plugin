package com.github.oassuncao.nexus.gitlab;

import com.github.oassuncao.nexus.gitlab.api.GitlabApiClient;
import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.sisu.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.security.role.RoleIdentifier;
import org.sonatype.nexus.security.user.*;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Silvio Assunção
 * @since 1.5
 */
@Singleton
@Named
@Description("Gitlab")
@Priority(Integer.MAX_VALUE)
public class GitlabUserManager extends AbstractUserManager implements RoleMappingUserManager {
// ------------------------------ FIELDS ------------------------------

    public final static String REALM_NAME = "gitlab";

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabUserManager.class);
    private final RoleMappingUserManager defaultRoleMappingUserManager;
    private GitlabApiClient client;
    private boolean enabled;
    private String url;
    private String token;
    private Duration cacheTtl;
    private String defaultRole;
    private String groupAdmin;
    private String roleAdmin;
    private String groupPusher;
    private String rolePusher;

// -------------------------- STATIC METHODS --------------------------

    private static User mapFrom(GitlabPrincipal principal) {
        User user = new User();
        user.setName(principal.getName());
        user.setUserId(principal.getUsername());
        user.setEmailAddress(principal.getEmail());
        user.setSource(REALM_NAME);
        UserStatus status = UserStatus.active;
        if (BooleanUtils.isTrue(principal.getBlocked()))
            status = UserStatus.disabled;
        user.setStatus(status);
        user.setRoles(getRolesIdentifier(principal));
        return user;
    }

    private static Set<RoleIdentifier> getRolesIdentifier(GitlabPrincipal principal) {
        return principal.getRoles().stream().map(d -> new RoleIdentifier(REALM_NAME, d)).collect(Collectors.toSet());
    }

// --------------------------- CONSTRUCTORS ---------------------------

    @Inject
    public GitlabUserManager(@Named("default") RoleMappingUserManager defaultRoleMappingUserManager, GitlabApiClient client) {
        this.defaultRoleMappingUserManager = defaultRoleMappingUserManager;
        this.client = client;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setCacheTtl(Duration cacheTtl) {
        this.cacheTtl = cacheTtl;
    }

    public void setDefaultRole(String defaultRole) {
        this.defaultRole = defaultRole;
    }

    public void setGroupAdmin(String groupAdmin) {
        this.groupAdmin = groupAdmin;
    }

    public void setGroupPusher(String groupPusher) {
        this.groupPusher = groupPusher;
    }

    public void setRoleAdmin(String roleAdmin) {
        this.roleAdmin = roleAdmin;
    }

    public void setRolePusher(String rolePusher) {
        this.rolePusher = rolePusher;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUrl(String url) {
        this.url = url;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface RoleMappingUserManager ---------------------

    @Override
    public Set<RoleIdentifier> getUsersRoles(String userId, String userSource) throws UserNotFoundException {
        LOGGER.trace("Getting Users Roles of {} {}", userId, userSource);
        if (!REALM_NAME.equals(userSource))
            return defaultRoleMappingUserManager.getUsersRoles(userId, userSource);

        try {
            GitlabPrincipal principal = client.findUser(userId);
            LOGGER.debug("Got principal {} with roles {} ", principal.getUsername(), principal.getRoles());
            return getRolesIdentifier(principal);
        } catch (GitlabAuthenticationException e) {
            throw new UserNotFoundException(userId, "Error on findUser", e);
        }
    }

    @Override
    public void setUsersRoles(String userId, String userSource, Set<RoleIdentifier> roles) throws UserNotFoundException {

    }

// --------------------- Interface UserManager ---------------------

    @Override
    public String getSource() {
        return REALM_NAME;
    }

    @Override
    public String getAuthenticationRealmName() {
        return REALM_NAME;
    }

    @Override
    public boolean supportsWrite() {
        return false;
    }

    @Override
    public Set<User> listUsers() {
        return new HashSet<>();
    }

    @Override
    public Set<String> listUserIds() {
        return new HashSet<>();
    }

    @Override
    public User addUser(User user, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public User updateUser(User user) throws UserNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteUser(String s) throws UserNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<User> searchUsers(UserSearchCriteria userSearchCriteria) {
        return new HashSet<>();
    }

    @Override
    public User getUser(String username) throws UserNotFoundException {
        LOGGER.trace("Getting User {}", username);
        try {
            GitlabPrincipal principal = findUser(username);
            return mapFrom(principal);
        } catch (GitlabAuthenticationException e) {
            throw new UserNotFoundException(username, "Error on findUser", e);
        }
    }

    @Override
    public void changePassword(String s, String s1) throws UserNotFoundException {
        throw new UnsupportedOperationException();
    }

// -------------------------- OTHER METHODS --------------------------

    public GitlabPrincipal authenticate(String login, char[] token) throws GitlabAuthenticationException {
        if (!enabled)
            throw new GitlabAuthenticationException("The Gitlab integration is disabled");

        GitlabPrincipal principal = client.authenticate(login, token);
        loadPrincipal(principal);
        return principal;
    }

    private void loadPrincipal(GitlabPrincipal principal) {
        principal.setRoles(getRoles(principal));
    }

    private Set<String> getRoles(GitlabPrincipal principal) {
        Set<String> roles = new HashSet<>();
        roles.add(defaultRole);
        if (principal.getGroups() != null) {
            if (principal.getGroups().stream().anyMatch(d -> groupAdmin.equals(d)))
                roles.add(roleAdmin);
            if (principal.getGroups().stream().anyMatch(d -> groupPusher.equals(d)))
                roles.add(rolePusher);
        }
        return roles;
    }

    public void disabled() {
        enabled = false;
    }

    public void enable() {
        enabled = true;
        client.init(url, token, cacheTtl);
    }

    public GitlabPrincipal findUser(String username) throws GitlabAuthenticationException {
        if (!enabled)
            throw new GitlabAuthenticationException("The Gitlab integration is disabled");

        GitlabPrincipal principal = client.findUser(username);
        loadPrincipal(principal);
        return principal;
    }

    public boolean isDisabled() {
        return !enabled;
    }
}
