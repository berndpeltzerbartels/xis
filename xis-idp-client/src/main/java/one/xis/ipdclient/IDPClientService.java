package one.xis.ipdclient;

import one.xis.security.UserInfo;
import one.xis.server.ApiTokens;

public interface IDPClientService {
    String getIDPLoginFormUrl(String idpId, String redirectUri);

    ApiTokens requestTokens(String idpId, String code, String state);

    ApiTokens renewTokens(String idpId, String refreshToken);

    UserInfo fetchUserInfoFromIdp(String idpId, String accessToken);

    UserInfo verifyAndDecodeToken(String idpId, String accessToken);
}
