package one.xis.auth;

import one.xis.context.AppContext;
import one.xis.http.RequestContext;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendService;
import one.xis.server.LocalUrlHolder;
import one.xis.server.ServerResponse;
import one.xis.server.UserSecurityServiceImpl;
import one.xis.auth.token.SecurityAttributes;
import one.xis.auth.token.TokenStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class LocalAuthenticationIntegrationTest {


    @Nested
    @DisplayName("Request without Token to Page without @Roles")
    class NoTokenNoRoles {

        private AppContext context;

        @BeforeEach
        void init() {
            context = AppContext.builder()
                    .withSingletonClass(TestUserInfoService.class)
                    .withXIS().build();
        }

        private ClientRequest clientRequest;

        @BeforeEach
        void setUp() {
            clientRequest = new ClientRequest();
            clientRequest.setPageId("/test.html");
            clientRequest.setClientId("test-client");
            clientRequest.setLocale(Locale.GERMANY);
            clientRequest.setZoneId("Europe/Berlin");
            RequestContext.createInstance(mock(), mock());
        }

        @Test
        void processModelDataRequest_shouldSucceed() {
            FrontendService frontendService = context.getSingleton(FrontendService.class);

            ServerResponse response = assertDoesNotThrow(() -> frontendService.processModelDataRequest(clientRequest));

            assertThat(response).isNotNull();
            assertThat(response.getData()).containsEntry("message", "Success");
        }
    }

    @Nested
    @DisplayName("Request with Invalid Token to Page with @Roles")
    class InvalidToken {

        private AppContext context;

        @BeforeEach
        void init() {
            context = AppContext.builder()
                    .withSingletonClass(TestUserInfoService.class)
                    .withXIS().build();
        }

        private ClientRequest clientRequest;

        @BeforeEach
        void setUp() {
            clientRequest = new ClientRequest();
            clientRequest.setPageId("/protected.html");
            clientRequest.setClientId("test-client");
            clientRequest.setLocale(Locale.GERMANY);
            clientRequest.setZoneId("Europe/Berlin");
            clientRequest.setAccessToken("this-is-an-invalid-token");
            RequestContext.createInstance(mock(), mock());
        }

        @Test
        void processModelDataRequest_shouldReturnForbidden() {
            FrontendService frontendService = context.getSingleton(FrontendService.class);

            assertThrows(URLForbiddenException.class, () -> frontendService.processModelDataRequest(clientRequest));
        }
    }


    @Nested
    @DisplayName("Local token renewal")
    class LocalRenew {

        private AppContext context;

        @BeforeEach
        void init() {
            context = AppContext.builder()
                    .withSingletonClass(TestUserInfoService.class)
                    .withXIS().build();
            context.getSingleton(LocalUrlHolder.class).setLocalUrl("http://localhost:4711");
            var userInfo = new UserInfoImpl();
            userInfo.setUserId("alice");
            userInfo.setRoles(Set.of("USER"));
            context.getSingleton(TestUserInfoService.class).saveUserInfo(userInfo, "secret");
        }

        @Test
        void expiredAccessTokenWithLocalUrlIssuerIsRenewedWithLocalRenewToken() {
            var localTokenService = context.getSingleton(LocalTokenService.class);
            var tokens = localTokenService.newTokens("alice");
            var expiredAccessToken = localTokenService.createToken(expiredAccessTokenClaims("alice"));
            var tokenStatus = new TokenStatus(expiredAccessToken, tokens.getRenewToken());
            var securityAttributes = new TestSecurityAttributes();

            assertThatThrownBy(() -> localTokenService.decodeAccessToken(expiredAccessToken))
                    .isInstanceOf(TokenExpiredException.class);

            context.getSingleton(UserSecurityServiceImpl.class).update(tokenStatus, securityAttributes);

            assertThat(securityAttributes.getUserId()).isEqualTo("alice");
            assertThat(securityAttributes.getRoles()).containsExactly("USER");
            assertThat(tokenStatus.isRenewed()).isTrue();
            assertThat(tokenStatus.getAccessToken()).isNotEqualTo(expiredAccessToken);
            assertThat(tokenStatus.getRenewToken()).isNotEqualTo(tokens.getRenewToken());
        }

        private AccessTokenClaims expiredAccessTokenClaims(String userId) {
            var claims = new AccessTokenClaims();
            claims.setUserId(userId);
            claims.setUsername(userId);
            claims.setJwtId("expired-token");
            claims.setIssuer("http://localhost:4711");
            claims.setResourceAccess(new AccessTokenClaims.ResourceAccess(new AccessTokenClaims.ResourceAccess.Account(Set.of("USER"))));
            claims.setRealmAccess(new AccessTokenClaims.RealmAccess(Set.of("USER")));
            claims.setExpiresAtSeconds(Instant.now().minusSeconds(60).getEpochSecond());
            claims.setIssuedAtSeconds(Instant.now().minusSeconds(120).getEpochSecond());
            claims.setNotBeforeSeconds(Instant.now().minusSeconds(120).getEpochSecond());
            claims.setClientId("xis-api");
            return claims;
        }
    }


    static class TestUserInfoService implements UserInfoService<UserInfo> {

        private final Collection<UserInfo> userInfos = new HashSet<>();
        private final Map<String, String> userPasswords = new HashMap<>();

        @Override
        public boolean validateCredentials(String userId, String password) {
            return userPasswords.getOrDefault(userId, "").equals(password);
        }

        @Override
        public Optional<UserInfo> getUserInfo(String userId) {
            return userInfos.stream()
                    .filter(userInfo -> userInfo.getUserId().equals(userId))
                    .findFirst();
        }

        public void saveUserInfo(UserInfo userInfo, String password) {
            saveUserInfo(userInfo);
            userPasswords.put(userInfo.getUserId(), password);
        }

        public void saveUserInfo(UserInfo userInfo) {
            getUserInfo(userInfo.getUserId()).map(UserInfoImpl.class::cast).ifPresentOrElse(
                    existingUserInfo -> existingUserInfo.setRoles(userInfo.getRoles()),
                    () -> userInfos.add(userInfo)
            );
        }
    }

    static class TestSecurityAttributes implements SecurityAttributes {

        private String userId;
        private Set<String> roles;

        @Override
        public String getUserId() {
            return userId;
        }

        @Override
        public Set<String> getRoles() {
            return roles;
        }

        @Override
        public void setUserId(String userId) {
            this.userId = userId;
        }

        @Override
        public void setRoles(Set<String> roles) {
            this.roles = roles;
        }
    }
}
