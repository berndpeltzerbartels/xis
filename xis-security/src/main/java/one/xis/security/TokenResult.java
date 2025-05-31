package one.xis.security;

import java.time.Instant;

public record TokenResult(String token, Instant expiresAt, String renewToken, Instant renewExpiresAt) {

}
