package one.xis.context;

import one.xis.auth.UserAccount;
import one.xis.auth.UserAccountImpl;
import one.xis.auth.UserAccountService;

import java.util.*;


class TestUserAccountService implements UserAccountService<UserAccount> {

    private final Collection<UserAccount> userAccounts = new HashSet<>();

    @Override
    public Optional<UserAccount> getUserAccount(String userId) {
        return userAccounts.stream()
                .filter(userAccount -> userAccount.getUserId().equals(userId))
                .findFirst();
    }

    public void saveUserAccount(UserAccount userAccount) {
        getUserAccount(userAccount.getUserId()).map(UserAccountImpl.class::cast).ifPresentOrElse(
                existingUserAccount -> existingUserAccount.setRoles(userAccount.getRoles()),
                () -> userAccounts.add(userAccount)
        );
    }
}
