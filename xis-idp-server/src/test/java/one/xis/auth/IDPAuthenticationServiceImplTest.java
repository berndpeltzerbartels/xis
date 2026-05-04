package one.xis.auth;

import one.xis.server.LocalUrlHolder;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IDPAuthenticationServiceImplTest {

    @Test
    void provideTokensUsesIDPServiceToValidateClientSecret() {
        var idpService = mock(IDPService.class);
        var service = new IDPAuthenticationServiceImpl(
                idpService,
                mock(LocalUrlHolder.class),
                new IDPCodeStore(),
                mock(LocalTokenService.class)
        );

        var request = new IDPTokenRequest();
        request.setClientId("orders-app");
        request.setClientSecret("orders-secret");
        request.setCode("code");
        request.setRedirectUri("http://localhost:8080/xis/auth/callback/xis-idp");

        when(idpService.findClientInfo("orders-app")).thenReturn(Optional.of(
                new IDPClientInfoImpl(
                        "orders-app",
                        "orders-secret",
                        Set.of("http://localhost:8080/xis/auth/callback/xis-idp")
                )
        ));
        when(idpService.validateClientSecret("orders-app", "orders-secret")).thenReturn(false);

        assertThrows(AuthenticationException.class, () -> service.provideTokens(request));
    }
}
