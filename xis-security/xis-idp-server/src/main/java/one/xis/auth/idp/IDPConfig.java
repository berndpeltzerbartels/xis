package one.xis.auth.idp;

import java.time.Duration;

public interface IDPConfig {

    Duration getAccessTokenValidity();

    Duration getRefreshTokenValidity();

    Duration getIdTokenValidity();
    
}
