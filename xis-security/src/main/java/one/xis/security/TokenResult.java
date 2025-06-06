package one.xis.security;

import java.time.Duration;

public record TokenResult(String accessToken, Duration accessTokenExpiresIn, String renewToken,
                          Duration renewTokenExpiresIn) {

}
