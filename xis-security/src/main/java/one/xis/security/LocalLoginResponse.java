package one.xis.security;

import lombok.Data;
import one.xis.server.ApiTokens;

@Data
public class LocalLoginResponse {
    private final ApiTokens tokens;
    private final String redirectUrl;
}
