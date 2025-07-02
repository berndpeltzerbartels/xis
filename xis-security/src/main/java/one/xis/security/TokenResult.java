package one.xis.security;

import java.time.Duration;


// TODO remove ?
public record TokenResult(String accessToken, Duration accessTokenExpiresIn, String renewToken,
                          Duration renewTokenExpiresIn) {

}
