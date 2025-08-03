package one.xis.auth;

import one.xis.server.RedirectResponse;

public class LoginResponse extends RedirectResponse {
    public LoginResponse(String state, String code) {
        super("/xis/auth/callback/local?state=" + state + "&code=" + code);
    }
}
