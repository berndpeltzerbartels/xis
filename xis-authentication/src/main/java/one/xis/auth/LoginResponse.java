package one.xis.auth;

import one.xis.server.RedirectResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LoginResponse extends RedirectResponse {
    public LoginResponse(String state, String code) {
        super("/xis/auth/callback/local?state=" + encode(state) + "&code=" + encode(code));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
