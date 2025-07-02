package one.xis.ipdclient;

import one.xis.auth.token.ApiTokens;
import one.xis.security.UserInfo;

public interface IDPClientService {
    String getIDPLoginFormUrl(String idpId, String redirectUri);

    ApiTokens fetchNewTokens(String idpId, String code, String state);

    ApiTokens fetchRenewedTokens(String idpId, String refreshToken);

    UserInfo fetchUserInfoFromIdp(String idpId, String accessToken);

    UserInfo verifyAndDecodeToken(String idpId, String accessToken);
}
