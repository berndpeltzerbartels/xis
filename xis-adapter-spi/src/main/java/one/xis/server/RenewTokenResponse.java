package one.xis.server;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RenewTokenResponse {
    private String accessToken;
    private long accessTokenExpiresAt;
    private String renewToken;
    private long renewTokenExpiresAt;
}
