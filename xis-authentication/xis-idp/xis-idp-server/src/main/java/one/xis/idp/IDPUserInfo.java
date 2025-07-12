package one.xis.idp;

import one.xis.auth.UserInfo;

public interface IDPUserInfo extends UserInfo {
    String getPassword();

    String getClientId();
}
