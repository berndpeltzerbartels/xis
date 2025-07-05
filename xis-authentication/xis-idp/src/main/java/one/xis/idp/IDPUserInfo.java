package one.xis.idp;

import one.xis.auth.UserInfo;

import java.util.Collection;

public interface IDPUserInfo extends UserInfo {
    String getPassword();

    Collection<String> getPermittedRedirectUrls();
}
