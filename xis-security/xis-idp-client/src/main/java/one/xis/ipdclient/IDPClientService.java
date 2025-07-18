package one.xis.ipdclient;

import one.xis.auth.UserInfoImpl;
import one.xis.auth.token.ApiTokens;

public interface IDPClientService {
    String loginFormUrl(String idpId, String redirectUri);

    ApiTokens fetchNewTokens(String idpId, String code, String state);

    ApiTokens fetchRenewedTokens(String idpId, String refreshToken);

    UserInfoImpl fetchUserInfoFromIdp(String idpId, String accessToken);

    UserInfoImpl verifyAndDecodeToken(String idpId, String accessToken);
}
