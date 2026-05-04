package one.xis.auth;

import one.xis.auth.idp.ExternalIDPService;
import one.xis.auth.idp.ExternalIDPServices;
import one.xis.context.AppContext;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class URLForbiddenExceptionHandlerTest {

    @Test
    void localLoginIsUsedWhenApplicationProvidesUserInfoService() {
        var appContext = mock(AppContext.class);
        var userInfoService = mock(UserInfoService.class);
        when(appContext.getOptionalSingleton(UserInfoService.class)).thenReturn(Optional.of(userInfoService));

        var handler = new URLForbiddenExceptionHandler(noExternalIdps(), appContext);

        var response = handler.handleException(null, new Object[0], new URLForbiddenException("/protected.html?mode=edit"));

        assertThat(response.getStatusCode()).isEqualTo(401);
        assertThat(response.getHeader("Location")).isEqualTo("/login.html?redirect_uri=%2Fprotected.html%3Fmode%3Dedit");
    }

    @Test
    void singleExternalIdpIsUsedDirectlyWhenNoLocalUserInfoServiceExists() {
        var appContext = mock(AppContext.class);
        when(appContext.getOptionalSingleton(UserInfoService.class)).thenReturn(Optional.empty());

        var externalIdp = externalIdp("https://idp.example/login");
        var handler = new URLForbiddenExceptionHandler(externalIdps(externalIdp), appContext);

        var response = handler.handleException(null, new Object[0], new URLForbiddenException("/protected.html"));

        assertThat(response.getStatusCode()).isEqualTo(401);
        assertThat(response.getHeader("Location")).isEqualTo("https://idp.example/login?redirect=%2Fprotected.html");
    }

    private static ExternalIDPServices noExternalIdps() {
        return externalIdps();
    }

    private static ExternalIDPServices externalIdps(ExternalIDPService... services) {
        return new ExternalIDPServices() {
            @Override
            public ExternalIDPService getServiceForIssuer(String issuer) {
                return null;
            }

            @Override
            public ExternalIDPService getExternalIDPService(String issuer) {
                return null;
            }

            @Override
            public Collection<ExternalIDPService> getExternalIDPServices() {
                return java.util.List.of(services);
            }
        };
    }

    private static ExternalIDPService externalIdp(String baseUrl) {
        return new ExternalIDPService() {
            @Override
            public String createLoginUrl(String postLoginRedirectUrl) {
                return baseUrl + "?redirect=" + java.net.URLEncoder.encode(postLoginRedirectUrl, java.nio.charset.StandardCharsets.UTF_8);
            }

            @Override
            public one.xis.auth.idp.ExternalIDPTokens fetchTokens(String code) {
                throw new UnsupportedOperationException();
            }

            @Override
            public one.xis.auth.idp.ExternalIDPTokens fetchRenewedTokens(String refreshToken) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String createStateParameter(String urlAfterLogin) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getProviderId() {
                return "test-idp";
            }

            @Override
            public String getIssuer() {
                return "test-issuer";
            }

            @Override
            public JsonWebKey getJsonWebKey(String kid) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
