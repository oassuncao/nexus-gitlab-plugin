package com.github.oassuncao.nexus.gitlab;

import com.github.oassuncao.nexus.gitlab.api.GitlabApiClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.gitlab.api.models.GitlabUser;
import org.sonatype.nexus.security.user.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Silvio Assunção
 * @since 1.5
 */
@Singleton
@Named
public class GitlabUserManager extends AbstractUserManager {
// ------------------------------ FIELDS ------------------------------

    public final static String REALM_NAME = "gitlab";
    private GitlabApiClient client;

// -------------------------- STATIC METHODS --------------------------

    private static User mapFrom(GitlabUser model) {
        User user = new User();
        user.setName(model.getName());
        user.setUserId(model.getUsername());
        user.setEmailAddress(model.getEmail());
        user.setSource(REALM_NAME);
        UserStatus status = UserStatus.active;
        if (BooleanUtils.isTrue(model.isBlocked()))
            status = UserStatus.disabled;
        user.setStatus(status);
        return user;
    }

// --------------------------- CONSTRUCTORS ---------------------------

    @Inject
    public GitlabUserManager(GitlabApiClient client) {
        this.client = client;
    }

// ------------------------ INTERFACE METHODS ------------------------


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
        return null;
    }

    @Override
    public User updateUser(User user) throws UserNotFoundException {
        return null;
    }

    @Override
    public void deleteUser(String s) throws UserNotFoundException {

    }

    @Override
    public Set<User> searchUsers(UserSearchCriteria userSearchCriteria) {
        return null;
    }

    @Override
    public User getUser(String username) throws UserNotFoundException {
        try {
            List<GitlabUser> users = client.findUser(username);
            if (CollectionUtils.isEmpty(users))
                throw new UserNotFoundException(username);

            if (users.size() > 1)
                throw new UserNotFoundException(username, "More than one user has this username", null);

            return mapFrom(users.get(0));
        } catch (GitlabAuthenticationException e) {
            throw new UserNotFoundException(username, "Error on get user data", e);
        }
    }

    @Override
    public void changePassword(String s, String s1) throws UserNotFoundException {

    }
}
