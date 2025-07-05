package one.xis.idp;

import org.junit.jupiter.api.BeforeEach;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

class AuthenticationIntegrationTest {

    private IDPUserInfoImpl userInfo;

    @BeforeEach
    void init() {
        userInfo = new IDPUserInfoImpl();
        userInfo.setUserId("user1");
        userInfo.setRoles(Set.of("user"));
        userInfo.setClaims(Map.of("email", "user1@example.com"));
    }

    class DummyUserService implements IPDUserService {

        @Override
        public Optional<IDPUserInfo> findUserInfo(String userId) {
            return Optional.empty();
        }
    }

    /* TODO
    @Test
    void integrationLocalAuthWithStateParameterVerification() throws AuthenticationException, InvalidTokenException {
        var userService = new DummyUserService();
        var localAuth = new IDPAuthenticationServiceImpl(userService);
        var connectionFactory = new ExternalIDPConnectionFactory();

        var providerConfig = mock(ExternalIDPConfig.class);
        when(providerConfig.getLoginFormUrl()).thenReturn("https://auth.example.com/authorize");
        when(providerConfig.getTokenEndpoint()).thenReturn("https://auth.example.com/token");
        var providerService = new IDPAuthenticationServiceImpl(userService);

        // Extract and verify state
        String stateParam = authorizationUrl.split("state=")[1].split("&")[0];
        String queryString = "code=someCode&state=" + stateParam;
        AuthenticationProviderStateData stateData = providerService.verifyAndDecodeCodeAndStateQuery(queryString);

        // Simulate login and token issue
        String code = localAuth.login(new IDPLogin("user1", "secret", "state", "/dashboard.html"));
        IDPTokens tokenResponse = localAuth.issueToken(code, stateData.getStateParameterPayload().getRedirect());

        // Assert
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.getAccessToken()).isNotEmpty();
        assertThat(tokenResponse.getState()).isEqualTo("/dashboard");
        assertThat(localAuth.getUserInfo(tokenResponse.getAccessToken())).isEqualTo(userInfo);
    }
    */
}
