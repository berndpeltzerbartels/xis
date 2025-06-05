package one.xis.security;

import java.time.Duration;

public record TokenResult(String accessToken, Duration accessTokenExpiresAt, String renewToken,
                          Duration renewTokenExpiresAt) {

}
