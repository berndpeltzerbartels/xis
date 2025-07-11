package one.xis.auth;

import lombok.Data;
import one.xis.auth.token.ApiTokens;
import one.xis.server.RedirectControllerResponse;
import one.xis.server.TokenResponse;

import java.util.Map;

@Data
public class LocalLoginResponse implements RedirectControllerResponse, TokenResponse {
    private final String redirectUrl;
    private final ApiTokens apiTokens;

    @Override
    public String toString() {
        return redirectUrl;
    }

    @Override
    public String getRedirectUrl() {
        return redirectUrl;
    }

    @Override
    public Map<String, Object> getUrlParameters() {
        return Map.of();
    }
}
