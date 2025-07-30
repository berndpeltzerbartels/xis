package one.xis.auth;

import java.time.Duration;

public interface IDPConfig {

    Duration getAccessTokenValidity();

    Duration getRefreshTokenValidity();

    Duration getIdTokenValidity();

}
