package one.xis.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationIntegrationTest {

    private LocalUserInfo userInfo;

    @BeforeEach
    void init() {
        userInfo = new LocalUserInfo();
        userInfo.setUserId("user1");
        userInfo.setRoles(Set.of("user"));
        userInfo.setClaims(Map.of("email", "user1@example.com"));
    }

    class DummyUserService implements UserService {
        @Override
        public boolean checkCredentials(String userId, String password) {
            return "user1".equals(userId) && "secret".equals(password);
        }

        @Override
        public LocalUserInfo getUserInfo(String userId) {
            if ("user1".equals(userId)) {
                return userInfo;
            }
            return null;
        }
    }

    @Test
    void integrationLocalAuthWithStateParameterVerification() throws AuthenticationException, InvalidTokenException {
        // Arrange
        var codeStore = new LocalAuthenticationCodeStore();
        var userService = new DummyUserService();
        var localAuth = new LocalAuthenticationProviderServiceImpl(userService);
        var connectionFactory = new AuthenticationProviderConnectionFactory();

        var providerConfig = new AuthenticationProviderConfiguration();
        providerConfig.setAuthenticationProviderId("dummy");
        providerConfig.setAuthorizationEndpoint("https://auth.example.com/authorize");
        providerConfig.setTokenEndpoint("https://auth.example.com/token");

        var providerService = new AuthenticationServiceImpl(providerConfig, connectionFactory);

        // Act
        String authorizationUrl = providerService.createAuthorizationUrl("/dashboard");
        assertThat(authorizationUrl).contains("state=");

        // Extract and verify state
        String stateParam = authorizationUrl.split("state=")[1].split("&")[0];
        String queryString = "code=someCode&state=" + stateParam;
        AuthenticationProviderStateData stateData = providerService.verifyStateAndExtractCode(queryString);

        // Simulate login and token issue
        String code = localAuth.login("user1", "secret");
        LocalAuthenticationTokenResponse tokenResponse = localAuth.issueToken(code, stateData.getStateParameterPayload().getRedirect());

        // Assert
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.getAccessToken()).isNotEmpty();
        assertThat(tokenResponse.getState()).isEqualTo("/dashboard");
        assertThat(localAuth.getUserInfo(tokenResponse.getAccessToken())).isEqualTo(userInfo);
    }
}
