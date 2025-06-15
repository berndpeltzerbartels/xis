package one.xis.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class LocalAuthenticationProviderServiceTest {

    private LocalAuthenticationProviderServiceImpl authentication;
    private LocalUserInfoService userService;

    @BeforeEach
    void setUp() {
        userService = new TestUserService();
        authentication = new LocalAuthenticationProviderServiceImpl(mock(), userService);
    }

    @Test
    void fullAuthenticationCycle() throws Exception {
        // Login
        String code = authentication.login(new Login("user1", "secret", "state"));
        assertThat(code).isNotBlank();

        // Token issuance
        LocalAuthenticationTokens tokenResponse = authentication.issueToken(code, "testState");
        assertThat(tokenResponse.getAccessToken()).isNotBlank();
        assertThat(tokenResponse.getRefreshToken()).isNotBlank();
        assertThat(tokenResponse.getState()).isEqualTo("testState");

        // Access token verification
        LocalUserInfo userInfo = authentication.getUserInfo(tokenResponse.getAccessToken());
        assertThat(userInfo.getUserId()).isEqualTo("user1");
        assertThat(userInfo.getRoles()).containsExactly("admin", "user");
        assertThat(userInfo.getClaims()).containsEntry("email", "user1@example.com");

        // Refresh
        LocalAuthenticationTokens refreshed = authentication.refresh(tokenResponse.getRefreshToken());
        assertThat(refreshed.getAccessToken()).isNotEqualTo(tokenResponse.getAccessToken());
        assertThat(refreshed.getRefreshToken()).isNotEqualTo(tokenResponse.getRefreshToken());
        assertThat(refreshed.getState()).isNull();
    }

    @Test
    void loginFailsOnWrongPassword() {
        assertThatThrownBy(() -> authentication.login(new Login("user1", "wrong", "state")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void issueTokenFailsOnInvalidCode() {
        assertThatThrownBy(() -> authentication.issueToken("invalid", "state"))
                .isInstanceOf(InvalidStateParameterException.class);
    }

    @Test
    void refreshFailsOnTamperedToken() {
        assertThatThrownBy(() -> authentication.refresh("invalid"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void shouldPreserveUserIdRolesAndClaimsAfterRefresh() throws Exception {
        // given
        String userId = "user1";
        Set<String> roles = Set.of("admin", "user");
        Map<String, Object> claims = Map.of("email", userId + "@example.com");

        String code = authentication.login(new Login("user1", "secret", "state"));
        LocalAuthenticationTokens initialToken = authentication.issueToken(code, "xyz");

        // when
        LocalAuthenticationTokens refreshedToken = authentication.refresh(initialToken.getRefreshToken());

        LocalUserInfo originalInfo = authentication.getUserInfo(initialToken.getAccessToken());
        LocalUserInfo refreshedInfo = authentication.getUserInfo(refreshedToken.getAccessToken());

        // then
        assertThat(refreshedInfo.getUserId()).isEqualTo(originalInfo.getUserId());
        assertThat(refreshedInfo.getRoles()).isEqualTo(originalInfo.getRoles());
        assertThat(refreshedInfo.getClaims()).isEqualTo(originalInfo.getClaims());
    }


    // Dummy UserService
    static class TestUserService implements LocalUserInfoService {

        @Override
        public boolean checkCredentials(String userId, String password) {
            return "user1".equals(userId) && "secret".equals(password);
        }

        @Override
        public LocalUserInfo getUserInfo(String userId) {
            LocalUserInfo info = new LocalUserInfo();
            info.setUserId(userId);
            info.setRoles(Set.of("admin", "user"));
            info.setClaims(Map.of("email", userId + "@example.com"));
            return info;
        }
    }

}
