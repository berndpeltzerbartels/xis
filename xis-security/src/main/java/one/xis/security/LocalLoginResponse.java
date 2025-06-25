package one.xis.security;

import lombok.Data;
import one.xis.server.ApiTokens;
import one.xis.server.RedirectControllerResponse;

import java.util.Map;

@Data
public class LocalLoginResponse implements RedirectControllerResponse {
    private final ApiTokens tokens;
    private final String redirectUrl;

    @Override
    public Map<String, Object> getUrlParameters() {
        return Map.of();
    }
}
