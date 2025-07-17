package one.xis.auth;

import one.xis.server.RedirectResponse;

public class LocalLoginResponse extends RedirectResponse {
    public LocalLoginResponse(String state, String code) {
        super("/xis/auth/callback/local?state=" + state + "&code=" + code);
    }
}
