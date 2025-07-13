package one.xis.auth.idp;

import one.xis.auth.UserInfo;

public interface IDPUserInfo extends UserInfo {
    String getClientId();
}
