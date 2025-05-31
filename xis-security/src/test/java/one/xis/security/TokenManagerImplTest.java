package one.xis.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenManagerImplTest {

    private TokenManagerImpl tokenManager;

    @BeforeEach
    void setUp() {
        // Tokens gelten je 1 Stunde / 24 Stunden
        tokenManager = new TokenManagerImpl(3600, 86400);
    }

    @Test
    void shouldCreateAndDecodeTokenSuccessfully() throws InvalidTokenException {
        TokenRequest request = new TokenRequest(
                "user123",
                List.of("ADMIN", "USER"),
                Map.of("customClaim", "abc"),
                3600,
                86400
        );

        TokenResult result = tokenManager.createTokens(request);

        assertThat(result.token()).isNotBlank();
        assertThat(result.expiresAt()).isAfter(Instant.now());

        TokenAttributes attributes = tokenManager.decodeToken(result.token());

        assertThat(attributes.userId()).isEqualTo("user123");
        assertThat(attributes.roles()).containsExactlyInAnyOrder("ADMIN", "USER");
        assertThat(attributes.claims().get("customClaim")).isEqualTo("abc");
        assertThat(attributes.expiresAt()).isAfter(Instant.now());
    }

    @Test
    void shouldRenewTokenAndContainSameClaims() throws InvalidTokenException {
        TokenRequest request = new TokenRequest(
                "user999",
                List.of("READER"),
                Map.of("info", "xyz"),
                3600,
                86400
        );

        TokenResult original = tokenManager.createTokens(request);
        TokenResult renewed = tokenManager.renew(original.token());

        assertThat(renewed.token()).isNotEqualTo(original.token());

        TokenAttributes renewedAttrs = tokenManager.decodeToken(renewed.token());
        assertThat(renewedAttrs.userId()).isEqualTo("user999");
        assertThat(renewedAttrs.roles()).containsExactly("READER");
        assertThat(renewedAttrs.claims().get("info")).isEqualTo("xyz");
    }

    @Test
    void shouldThrowOnInvalidTokenFormat() {
        assertThatThrownBy(() -> tokenManager.decodeToken("not.a.jwt"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid signature");
    }

    @Test
    void shouldThrowOnInvalidSignature() {
        TokenRequest request = new TokenRequest(
                "hacker",
                List.of(),
                Map.of(),
                3600,
                86400
        );
        String token = tokenManager.createTokens(request).token();
        String tampered = token.replaceFirst("\\..*?\\.", ".tampered.");

        assertThatThrownBy(() -> tokenManager.decodeToken(tampered))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid signature");
    }
}
