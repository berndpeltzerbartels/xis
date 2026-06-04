package one.xis.auth;

import one.xis.context.DefaultComponent;

import java.util.Optional;

@DefaultComponent
public class UserServicePlaceholder implements UserAccountService<UserAccountImpl> {
    @Override
    public Optional<UserAccountImpl> getUserAccount(String userId) {
        return Optional.empty();
    }

    @Override
    public void saveUserAccount(UserAccountImpl userAccount) {

    }
}
