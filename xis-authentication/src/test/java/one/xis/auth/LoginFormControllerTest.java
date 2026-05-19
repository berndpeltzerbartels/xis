package one.xis.auth;

import one.xis.server.ClientConfig;
import one.xis.server.ClientConfigService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginFormControllerTest {

    @Test
    void keepsLocalRedirectUriInStateParameter() {
        var controller = controllerWithWelcomePage("/home.html");

        LoginData login = controller.createLoginFormData("/customers.html?tab=open");

        assertThat(StateParameter.decode(login.getState()).getRedirect()).isEqualTo("/customers.html?tab=open");
    }

    @Test
    void rejectsAbsoluteRedirectUriInStateParameter() {
        var controller = controllerWithWelcomePage("/home.html");

        LoginData login = controller.createLoginFormData("https://evil.example/after-login");

        assertThat(StateParameter.decode(login.getState()).getRedirect()).isEqualTo("/home.html");
    }

    @Test
    void rejectsSchemeRelativeRedirectUriInStateParameter() {
        var controller = controllerWithWelcomePage("/home.html");

        LoginData login = controller.createLoginFormData("//evil.example/after-login");

        assertThat(StateParameter.decode(login.getState()).getRedirect()).isEqualTo("/home.html");
    }

    private LoginFormController<UserInfo> controllerWithWelcomePage(String welcomePageId) {
        ClientConfigService clientConfigService = mock(ClientConfigService.class);
        when(clientConfigService.getConfig()).thenReturn(new ClientConfig(
                List.of(),
                List.of(),
                List.of(),
                0,
                welcomePageId,
                Map.of(),
                Map.of()
        ));
        return new LoginFormController<>(
                mock(),
                mock(),
                mock(),
                mock(),
                clientConfigService,
                List.of(),
                mock()
        );
    }
}
