package one.xis.security;

import java.time.Instant;

public record TokenResult(String accessToken, Instant accessTokenExpiresAt, String renewToken,
                          Instant renewTokenExpiresAt) {

}
