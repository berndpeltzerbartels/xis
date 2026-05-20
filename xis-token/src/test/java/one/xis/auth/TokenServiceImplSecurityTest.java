package one.xis.auth;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceImplSecurityTest {

    private final TokenServiceImpl tokenService = new TokenServiceImpl(new Gson());
    private final LocalInMemoryKeyProvider keyProvider = new LocalInMemoryKeyProvider();
    private final String keyId = keyProvider.getKeyIds().iterator().next();
    private final KeyPair keyPair = keyProvider.getKeyPair(keyId);
    private final JsonWebKey jsonWebKey = keyProvider.getJsonWebKey(keyId);

    @Test
    void rejectsUnsupportedJwtAlgorithmBeforeAcceptingToken() {
        String token = tokenService.createToken(validClaims(), keyId, keyPair);
        String tamperedToken = replaceHeader(token, "{\"alg\":\"none\",\"typ\":\"JWT\",\"kid\":\"" + keyId + "\"}");

        assertThatThrownBy(() -> tokenService.decodeAccessToken(tamperedToken, jsonWebKey))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Unsupported token algorithm");
    }

    @Test
    void rejectsUnsupportedJwtType() {
        String token = tokenService.createToken(validClaims(), keyId, keyPair);
        String tamperedToken = replaceHeader(token, "{\"alg\":\"RS256\",\"typ\":\"NJWT\",\"kid\":\"" + keyId + "\"}");

        assertThatThrownBy(() -> tokenService.decodeAccessToken(tamperedToken, jsonWebKey))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Unsupported token type");
    }

    private AccessTokenClaims validClaims() {
        var claims = new AccessTokenClaims();
        claims.setUserId("mara");
        claims.setIssuer("https://issuer.example");
        claims.setExpiresAtSeconds(Instant.now().plusSeconds(300).getEpochSecond());
        claims.setIssuedAtSeconds(Instant.now().getEpochSecond());
        claims.setNotBeforeSeconds(Instant.now().minusSeconds(1).getEpochSecond());
        claims.setClientId("xis-api");
        return claims;
    }

    private String replaceHeader(String token, String headerJson) {
        String[] parts = token.split("\\.");
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
        return header + "." + parts[1] + "." + parts[2];
    }
}
