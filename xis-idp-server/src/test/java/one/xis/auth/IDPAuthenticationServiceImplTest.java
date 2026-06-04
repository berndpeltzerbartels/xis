package one.xis.auth;

import one.xis.server.LocalUrlHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IDPAuthenticationServiceImplTest {

    private static final String REDIRECT_URI = "http://localhost:8080/xis/auth/callback/xis-idp";

    private IDPService idpService;
    private LocalUrlHolder localUrlHolder;
    private IDPCodeStore idpCodeStore;
    private LocalTokenService localTokenService;
    private IDPCredentialService credentialService;
    private IDPAuthenticationServiceImpl service;

    @BeforeEach
    void setUp() {
        idpService = mock(IDPService.class);
        localUrlHolder = mock(LocalUrlHolder.class);
        idpCodeStore = new IDPCodeStore();
        localTokenService = mock(LocalTokenService.class);
        credentialService = mock(IDPCredentialService.class);
        service = new IDPAuthenticationServiceImpl(
                idpService,
                localUrlHolder,
                idpCodeStore,
                localTokenService,
                credentialService
        );
    }

    @Test
    void loginUsesCredentialServiceToValidateUserPassword() throws InvalidCredentialsException {
        var login = new IDPServerLogin();
        login.setUsername("alice");
        login.setPassword("secret");

        when(credentialService.validateUserCredentials("alice", "secret")).thenReturn(true);

        var code = service.login(login);

        assertThat(idpCodeStore.getUserIdForCode(code)).isEqualTo("alice");
    }

    @Test
    void loginRejectsInvalidUserPassword() {
        var login = new IDPServerLogin();
        login.setUsername("alice");
        login.setPassword("wrong");

        when(credentialService.validateUserCredentials("alice", "wrong")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> service.login(login));
    }

    @Test
    void provideTokensUsesCredentialServiceToValidateClientSecret() {
        var request = new IDPTokenRequest();
        request.setClientId("orders-app");
        request.setClientSecret("orders-secret");
        request.setCode("code");
        request.setRedirectUri(REDIRECT_URI);

        when(idpService.findClientInfo("orders-app")).thenReturn(Optional.of(
                new IDPClientInfoImpl(
                        "orders-app",
                        Set.of(REDIRECT_URI)
                )
        ));
        when(credentialService.validateClientSecret("orders-app", "orders-secret")).thenReturn(false);

        assertThrows(AuthenticationException.class, () -> service.provideTokens(request));
    }

    @Test
    void provideTokensUsesConfiguredValidityForEachTokenType() {
        var config = new TestIDPConfig(
                Duration.ofMinutes(11),
                Duration.ofDays(180),
                Duration.ofMinutes(3)
        );
        var request = new IDPTokenRequest();
        request.setClientId("tv-app");
        request.setClientSecret("tv-secret");
        request.setCode("login-code");
        request.setRedirectUri(REDIRECT_URI);

        idpCodeStore.store("login-code", "alice");
        when(localUrlHolder.getUrl()).thenReturn("https://idp.example");
        when(idpService.getConfig()).thenReturn(config);
        when(idpService.findClientInfo("tv-app")).thenReturn(Optional.of(
                new IDPClientInfoImpl("tv-app", Set.of(REDIRECT_URI))
        ));
        when(credentialService.validateClientSecret("tv-app", "tv-secret")).thenReturn(true);
        when(idpService.userAccount("alice")).thenReturn(Optional.of(new IDPUserInfoImpl("alice", "tv-app")));
        when(idpService.accessTokenClaims("alice")).thenReturn(Optional.of(new AccessTokenClaims()));
        when(idpService.idTokenClaims("alice")).thenReturn(Optional.of(new IDTokenClaims()));
        when(idpService.renewTokenClaims("alice")).thenReturn(new RenewTokenClaims());
        when(localTokenService.createToken(any(TokenClaims.class))).thenAnswer(invocation ->
                invocation.getArgument(0).getClass().getSimpleName());

        var response = service.provideTokens(request);

        assertThat(response.getAccessToken()).isEqualTo("AccessTokenClaims");
        assertThat(response.getIdToken()).isEqualTo("IDTokenClaims");
        assertThat(response.getRefreshToken()).isEqualTo("RenewTokenClaims");
        assertThat(response.getExpiresIn()).isEqualTo(config.getAccessTokenValidity().getSeconds());
        assertThat(response.getRefreshExpiresIn()).isEqualTo(config.getRefreshTokenValidity().getSeconds());

        var tokenCaptor = ArgumentCaptor.forClass(TokenClaims.class);
        verify(localTokenService, times(3)).createToken(tokenCaptor.capture());

        assertValidity(tokenCaptor.getAllValues().get(0), config.getAccessTokenValidity());
        assertValidity(tokenCaptor.getAllValues().get(1), config.getIdTokenValidity());
        assertValidity(tokenCaptor.getAllValues().get(2), config.getRefreshTokenValidity());
        assertThat(tokenCaptor.getAllValues())
                .allSatisfy(claims -> {
                    assertThat(claims.getUserId()).isEqualTo("alice");
                    assertThat(claims.getClientId()).isEqualTo("tv-app");
                    assertThat(claims.getIssuer()).isEqualTo("https://idp.example");
                    assertThat(claims.getNotBeforeSeconds()).isEqualTo(claims.getIssuedAtSeconds());
                });
    }

    private void assertValidity(TokenClaims claims, Duration validity) {
        assertThat(claims.getExpiresAtSeconds() - claims.getIssuedAtSeconds())
                .isEqualTo(validity.getSeconds());
    }

    private record TestIDPConfig(Duration accessTokenValidity,
                                 Duration refreshTokenValidity,
                                 Duration idTokenValidity) implements IDPConfig {
        @Override
        public Duration getAccessTokenValidity() {
            return accessTokenValidity;
        }

        @Override
        public Duration getRefreshTokenValidity() {
            return refreshTokenValidity;
        }

        @Override
        public Duration getIdTokenValidity() {
            return idTokenValidity;
        }
    }
}
