package one.xis.server;

import lombok.Data;

@Data
public class RedirectResponse implements RedirectControllerResponse {
    private final String redirectUrl;
}
