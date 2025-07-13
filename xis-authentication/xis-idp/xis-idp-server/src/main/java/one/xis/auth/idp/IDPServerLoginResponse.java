package one.xis.auth.idp;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import one.xis.server.RedirectControllerResponse;

import java.util.Map;

@Data
@RequiredArgsConstructor
public class IDPServerLoginResponse implements RedirectControllerResponse {
    /**
     * The tokens to be used for API access.
     */
    private final String loginSuccessRedirectUrl;
    private final String state;
    private final String code;

    @Override
    public String toString() {
        return getRedirectUrl();
    }

    public String getRedirectUrl() {
        StringBuilder sb = new StringBuilder(loginSuccessRedirectUrl);
        if (!loginSuccessRedirectUrl.contains("?")) {
            sb.append("?").append("code=").append(code).append("&state=").append(state);
        } else {
            if (!loginSuccessRedirectUrl.endsWith("&")) {
                sb.append("&");
            }
            sb.append("code=").append(code).append("&state=").append(state);
        }
        return sb.toString();
    }

    @Override
    public Map<String, Object> getUrlParameters() {
        return Map.of("state", state, "code", code);
    }
}
