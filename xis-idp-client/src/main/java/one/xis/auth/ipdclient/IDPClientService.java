package one.xis.auth.ipdclient;

import one.xis.auth.ApiTokens;
import one.xis.auth.UserInfoImpl;

public interface IDPClientService {
    String loginFormUrl(String idpId, String redirectUri);

    ApiTokens fetchNewTokens(String idpId, String code, String state);

    ApiTokens fetchRenewedTokens(String idpId, String refreshToken);

    UserInfoImpl fetchUserInfoFromIdp(String idpId, String accessToken);

    UserInfoImpl verifyAndDecodeToken(String idpId, String accessToken);
}
