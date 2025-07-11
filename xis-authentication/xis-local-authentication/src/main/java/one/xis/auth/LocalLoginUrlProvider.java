package one.xis.auth;

import one.xis.LoginUrlProvider;
import one.xis.context.XISDefaultComponent;

@XISDefaultComponent
class LocalLoginUrlProvider implements LoginUrlProvider {

    static final String LOGIN_URL = "/login.html";

    @Override
    public String loginUrl(String redirectUri) {
        return LOGIN_URL;
    }
}
