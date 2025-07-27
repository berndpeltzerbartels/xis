package one.xis.auth.idp;

import lombok.Data;

import java.time.Duration;

@Data
public class DefaultIDPConfig implements IDPConfig {

    private final Duration accessTokenValidity = Duration.ofMinutes(15);
    private final Duration refreshTokenValidity = Duration.ofDays(30);
    private final Duration idTokenValidity = Duration.ofMinutes(2);
}
