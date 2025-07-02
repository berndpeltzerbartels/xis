package one.xis.ipdclient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.security.SecurityUtil;

@Getter
@RequiredArgsConstructor
public class LocalIDPClientConfig implements IDPClientConfig {
    private final String idpId = "local-idp";
    private final String idpServerUrl;
    private final String clientId = SecurityUtil.createRandomKey(20);
    private final String clientSecret = SecurityUtil.createRandomKey(32);
}
