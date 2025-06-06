package one.xis.security;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class AuthenticationServiceImplTest {

    private AuthenticationServiceImpl service;

    @BeforeEach
    void setup() {
        AuthenticationProviderConnectionFactory connectionFactory = mock(AuthenticationProviderConnectionFactory.class);
        AuthenticationProviderConfiguration providerConfiguration = new AuthenticationProviderConfiguration();
        providerConfiguration.setAuthorizationEndpoint("https://provider/auth");
        providerConfiguration.setTokenEndpoint("https://provider/token");
        providerConfiguration.setClientId("my-client-id");
        providerConfiguration.setClientSecret("my-secret");
        providerConfiguration.setAuthenticationProviderId("my-provider");
        providerConfiguration.setApplicationRootEndpoint("https://myapp");
        service = new AuthenticationServiceImpl(providerConfiguration, connectionFactory);
    }

    @Test
    void createLoginUrl_containsRequiredParameters() {
        String url = service.createAuthorizationUrl();
        assertThat(url).contains("response_type=code")
                .contains("redirect_uri=https://myapp/xis/auth/my-provider")
                .contains("client_id=my-client-id")
                .contains("client_secret=my-secret")
                .contains("state=")
                .contains("nonce=");
    }

    @Test
    void verifyStateAndExtractCode_validState_returnsCode() {
        // valid signed state payload
        var payload = new StateParameterPayload();
        payload.setCsrf("csrf");
        payload.setRedirect("https://bla");
        payload.setIat(Instant.now().getEpochSecond());
        payload.setExpiresAtSeconds(Instant.now().getEpochSecond() + 2000);
        String statePayload = new Gson().toJson(payload);
        String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(statePayload.getBytes());
        String signature = SecurityUtil.signHmacSHA256(encodedPayload, getStateKey());
        String state = encodedPayload + "." + signature;

        String url = "https://myapp/callback?code=authCode123&state=" + state;

        AuthenticationProviderStateData stateData = service.verifyStateAndExtractCode(url);

        assertThat(stateData.getCode()).isEqualTo("authCode123");
    }

    @Test
    void verifyStateAndExtractCode_invalidState_throwsException() {
        String fakeState = "invalid.state.signature";
        String url = "https://myapp/callback?code=authCode123&state=" + fakeState;

        assertThatThrownBy(() -> service.verifyStateAndExtractCode(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid state parameter format");
    }

    private String getStateKey() {
        // Reflection workaround for accessing private final field
        try {
            var field = AuthenticationServiceImpl.class.getDeclaredField("stateSignatureKey");
            field.setAccessible(true);
            return (String) field.get(service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
