package one.xis.auth;

import one.xis.auth.idp.ExternalIDPService;
import one.xis.auth.idp.ExternalIDPServices;
import one.xis.auth.idp.ExternalIDPTokens;
import one.xis.context.AppContext;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthenticationControllerTest {

    @Test
    void externalLoginWithCustomUserInfoServiceIssuesLocalApplicationTokens() {
        var issuer = "https://accounts.google.com";
        var state = StateParameter.create("/protected.html", issuer);
        var tokenService = mock(TokenService.class);
        var localTokenService = mock(LocalTokenService.class);
        var appContext = mock(AppContext.class);
        var externalIDPServices = mock(ExternalIDPServices.class);
        var externalIDPService = mock(ExternalIDPService.class);
        var codeStore = mock(CodeStore.class);
        var userInfoService = new SavingUserInfoService();
        var jsonWebKey = mock(JsonWebKey.class);

        var externalTokens = new ExternalIDPTokens();
        externalTokens.setAccessToken("google-access-token");
        externalTokens.setRefreshToken("google-refresh-token");
        externalTokens.setIdToken("google-id-token");
        externalTokens.setExpiresInSeconds(60);
        externalTokens.setRefreshExpiresInSeconds(120);

        var idTokenClaims = new IDTokenClaims();
        idTokenClaims.setUserId("google-sub");
        idTokenClaims.setEmail("user@example.test");
        idTokenClaims.setEmailVerified(true);

        var localTokens = new ApiTokens("local-access-token", Duration.ofMinutes(5), "local-renew-token", Duration.ofHours(1));

        when(externalIDPServices.getExternalIDPService(issuer)).thenReturn(externalIDPService);
        when(externalIDPService.fetchTokens("code")).thenReturn(externalTokens);
        when(externalIDPService.getJsonWebKey("kid")).thenReturn(jsonWebKey);
        when(appContext.getOptionalSingleton(UserInfoService.class)).thenReturn(Optional.of(userInfoService));
        when(tokenService.extractKeyId("google-id-token")).thenReturn("kid");
        when(tokenService.decodeIdToken("google-id-token", jsonWebKey)).thenReturn(idTokenClaims);
        when(localTokenService.newTokens(any(UserInfo.class))).thenReturn(localTokens);

        var controller = new AuthenticationController(tokenService, localTokenService, appContext, externalIDPServices, codeStore);

        var response = controller.authenticationCallback("code", state);

        assertThat(response.getHeader("Location")).isEqualTo("/protected.html");
        assertThat(response.getHeaders("Set-Cookie"))
                .anySatisfy(cookie -> assertThat(cookie).startsWith("access_token=local-access-token"))
                .anySatisfy(cookie -> assertThat(cookie).startsWith("refresh_token=local-renew-token"));
        assertThat(userInfoService.saved.get()).isNotNull();
        assertThat(userInfoService.saved.get().getUserId()).isEqualTo("google-sub");
        assertThat(userInfoService.saved.get().getRoles()).containsExactly("USER");
        verify(localTokenService).newTokens(userInfoService.saved.get());
    }

    @Test
    void externalLoginWithoutUserInfoServiceIssuesLocalApplicationTokensWithEmptyRoles() {
        var issuer = "https://accounts.google.com";
        var state = StateParameter.create("/community.html", issuer);
        var tokenService = mock(TokenService.class);
        var localTokenService = mock(LocalTokenService.class);
        var appContext = mock(AppContext.class);
        var externalIDPServices = mock(ExternalIDPServices.class);
        var externalIDPService = mock(ExternalIDPService.class);
        var codeStore = mock(CodeStore.class);
        var jsonWebKey = mock(JsonWebKey.class);

        var externalTokens = new ExternalIDPTokens();
        externalTokens.setAccessToken("google-access-token");
        externalTokens.setRefreshToken("google-refresh-token");
        externalTokens.setIdToken("google-id-token");
        externalTokens.setExpiresInSeconds(60);
        externalTokens.setRefreshExpiresInSeconds(120);

        var idTokenClaims = new IDTokenClaims();
        idTokenClaims.setUserId("google-sub");
        idTokenClaims.setName("Google User");
        idTokenClaims.setEmail("user@example.test");
        idTokenClaims.setEmailVerified(true);

        var localTokens = new ApiTokens("local-access-token", Duration.ofMinutes(5), "local-renew-token", Duration.ofHours(1));
        var capturedUserInfo = new AtomicReference<UserInfo>();

        when(externalIDPServices.getExternalIDPService(issuer)).thenReturn(externalIDPService);
        when(externalIDPService.fetchTokens("code")).thenReturn(externalTokens);
        when(externalIDPService.getJsonWebKey("kid")).thenReturn(jsonWebKey);
        when(appContext.getOptionalSingleton(UserInfoService.class)).thenReturn(Optional.empty());
        when(tokenService.extractKeyId("google-id-token")).thenReturn("kid");
        when(tokenService.extractKeyId("google-access-token")).thenThrow(new InvalidTokenException("opaque access token"));
        when(tokenService.decodeIdToken("google-id-token", jsonWebKey)).thenReturn(idTokenClaims);
        when(localTokenService.newTokens(any(UserInfo.class))).thenAnswer(invocation -> {
            capturedUserInfo.set(invocation.getArgument(0));
            return localTokens;
        });

        var controller = new AuthenticationController(tokenService, localTokenService, appContext, externalIDPServices, codeStore);

        var response = controller.authenticationCallback("code", state);

        assertThat(response.getHeader("Location")).isEqualTo("/community.html");
        assertThat(response.getHeaders("Set-Cookie"))
                .anySatisfy(cookie -> assertThat(cookie).startsWith("access_token=local-access-token"))
                .anySatisfy(cookie -> assertThat(cookie).startsWith("refresh_token=local-renew-token"));
        assertThat(capturedUserInfo.get().getUserId()).isEqualTo("google-sub");
        assertThat(capturedUserInfo.get().getName()).isEqualTo("Google User");
        assertThat(capturedUserInfo.get().getRoles()).isEmpty();
        verify(localTokenService).newTokens(any(UserInfo.class));
    }

    @Test
    void externalLoginWithoutUserInfoServiceCopiesRolesFromReadableExternalAccessToken() {
        var issuer = "http://localhost:8080/realms/xis";
        var state = StateParameter.create("/protected.html", issuer);
        var tokenService = mock(TokenService.class);
        var localTokenService = mock(LocalTokenService.class);
        var appContext = mock(AppContext.class);
        var externalIDPServices = mock(ExternalIDPServices.class);
        var externalIDPService = mock(ExternalIDPService.class);
        var codeStore = mock(CodeStore.class);
        var idTokenJsonWebKey = mock(JsonWebKey.class);
        var accessTokenJsonWebKey = mock(JsonWebKey.class);

        var externalTokens = new ExternalIDPTokens();
        externalTokens.setAccessToken("keycloak-access-token");
        externalTokens.setRefreshToken("keycloak-refresh-token");
        externalTokens.setIdToken("keycloak-id-token");
        externalTokens.setExpiresInSeconds(60);
        externalTokens.setRefreshExpiresInSeconds(120);

        var idTokenClaims = new IDTokenClaims();
        idTokenClaims.setUserId("keycloak-sub");
        idTokenClaims.setPreferredUsername("keycloak-user");

        var accessTokenClaims = new AccessTokenClaims();
        accessTokenClaims.setRoles(Set.of("USER"));

        var localTokens = new ApiTokens("local-access-token", Duration.ofMinutes(5), "local-renew-token", Duration.ofHours(1));
        var capturedUserInfo = new AtomicReference<UserInfo>();

        when(externalIDPServices.getExternalIDPService(issuer)).thenReturn(externalIDPService);
        when(externalIDPService.fetchTokens("code")).thenReturn(externalTokens);
        when(externalIDPService.getJsonWebKey("id-kid")).thenReturn(idTokenJsonWebKey);
        when(externalIDPService.getJsonWebKey("access-kid")).thenReturn(accessTokenJsonWebKey);
        when(appContext.getOptionalSingleton(UserInfoService.class)).thenReturn(Optional.empty());
        when(tokenService.extractKeyId("keycloak-id-token")).thenReturn("id-kid");
        when(tokenService.extractKeyId("keycloak-access-token")).thenReturn("access-kid");
        when(tokenService.decodeIdToken("keycloak-id-token", idTokenJsonWebKey)).thenReturn(idTokenClaims);
        when(tokenService.decodeAccessToken("keycloak-access-token", accessTokenJsonWebKey)).thenReturn(accessTokenClaims);
        when(localTokenService.newTokens(any(UserInfo.class))).thenAnswer(invocation -> {
            capturedUserInfo.set(invocation.getArgument(0));
            return localTokens;
        });

        var controller = new AuthenticationController(tokenService, localTokenService, appContext, externalIDPServices, codeStore);

        var response = controller.authenticationCallback("code", state);

        assertThat(response.getHeader("Location")).isEqualTo("/protected.html");
        assertThat(capturedUserInfo.get().getUserId()).isEqualTo("keycloak-sub");
        assertThat(capturedUserInfo.get().getPreferredUsername()).isEqualTo("keycloak-user");
        assertThat(capturedUserInfo.get().getRoles()).containsExactly("USER");
        verify(localTokenService).newTokens(any(UserInfo.class));
    }

    private static class SavingUserInfoService implements UserInfoService<UserInfoImpl> {

        private final AtomicReference<UserInfoImpl> saved = new AtomicReference<>();

        @Override
        public boolean validateCredentials(String userId, String password) {
            return false;
        }

        @Override
        public Optional<UserInfoImpl> getUserInfo(String userId) {
            return Optional.empty();
        }

        @Override
        public void saveUserInfo(UserInfoImpl userInfo) {
            userInfo.setRoles(Set.of("USER"));
            saved.set(userInfo);
        }
    }
}
