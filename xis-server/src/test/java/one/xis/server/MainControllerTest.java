package one.xis.server;

import one.xis.http.HttpRequest;
import one.xis.http.HttpResponse;
import one.xis.http.SseEndpoint;
import one.xis.auth.AuthenticationException;
import one.xis.auth.token.SecurityAttributes;
import one.xis.auth.token.TokenStatus;
import one.xis.auth.token.UserSecurityService;
import org.junit.jupiter.api.Test;
import one.xis.validation.ValidatorMessageResolver;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MainControllerTest {

    @Test
    void subscribeToEventsUsesQueryParameterWhenHeaderIsMissing() {
        var frontendService = mock(FrontendService.class);
        var sseEndpoint = mock(SseEndpoint.class);
        var userSecurityService = mock(UserSecurityService.class);
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        var controller = controller(frontendService, sseEndpoint, userSecurityService);

        controller.subscribeToEvents(null, "client-123", null, null, request, response);

        verify(sseEndpoint).open("client-123", null, request, response);
        verify(response, never()).setStatusCode(400);
    }

    @Test
    void subscribeToEventsReturnsBadRequestWhenClientIdIsMissingEverywhere() {
        var frontendService = mock(FrontendService.class);
        var sseEndpoint = mock(SseEndpoint.class);
        var userSecurityService = mock(UserSecurityService.class);
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        var controller = controller(frontendService, sseEndpoint, userSecurityService);

        controller.subscribeToEvents("  ", null, null, null, request, response);

        verify(response).setStatusCode(400);
        verify(response).setBody("Missing clientId");
        verifyNoInteractions(sseEndpoint);
    }

    @Test
    void subscribeToEventsWritesRenewedTokenCookiesBeforeOpeningSse() {
        var frontendService = mock(FrontendService.class);
        var sseEndpoint = mock(SseEndpoint.class);
        var userSecurityService = mock(UserSecurityService.class);
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        var controller = controller(frontendService, sseEndpoint, userSecurityService);

        doAnswer(invocation -> {
            TokenStatus tokenStatus = invocation.getArgument(0);
            SecurityAttributes attributes = invocation.getArgument(1);
            tokenStatus.setAccessToken("new-access-token");
            tokenStatus.setRenewToken("new-refresh-token");
            tokenStatus.setExpiresIn(Duration.ofMinutes(5));
            tokenStatus.setRenewExpiresIn(Duration.ofHours(1));
            attributes.setUserId("user-1");
            return null;
        }).when(userSecurityService).update(any(), any());

        controller.subscribeToEvents(null, "client-123", "old-access-token", "old-refresh-token", request, response);

        verify(response).addSecureCookie("access_token", "new-access-token", Duration.ofMinutes(5));
        verify(response).addSecureCookie("refresh_token", "new-refresh-token", Duration.ofHours(1));
        verify(sseEndpoint).open("client-123", "user-1", request, response);
    }

    @Test
    void subscribeToEventsReturnsUnauthorizedWhenTokenCannotBeAuthenticated() {
        var frontendService = mock(FrontendService.class);
        var sseEndpoint = mock(SseEndpoint.class);
        var userSecurityService = mock(UserSecurityService.class);
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        var controller = controller(frontendService, sseEndpoint, userSecurityService);

        doThrow(new AuthenticationException()).when(userSecurityService).update(any(), any());

        controller.subscribeToEvents(null, "client-123", "expired-access-token", "invalid-refresh-token", request, response);

        verify(response).setStatusCode(401);
        verifyNoInteractions(sseEndpoint);
    }

    private MainController controller(FrontendService frontendService, SseEndpoint sseEndpoint, UserSecurityService userSecurityService) {
        return new MainController(frontendService, sseEndpoint, userSecurityService, mock(ValidatorMessageResolver.class));
    }
}
