package one.xis.auth;

import one.xis.context.XISComponent;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@XISComponent
class LocalLoginUrlProvider implements LoginUrlProvider {

    static final String LOGIN_URL = "/login.html";

    @Override
    public String loginUrl(String redirectUri) {
        return LOGIN_URL + "?redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
    }
}
