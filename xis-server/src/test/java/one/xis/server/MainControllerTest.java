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
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MainControllerTest {

    @Test
    void subscribeToEventsUsesQueryParameterWhenHeaderIsMissing() {
        var frontendService = mock(FrontendService.class);
        var sseEndpoint = mock(SseEndpoint.class);
        var sseService = mock(SseService.class);
        var userSecurityService = mock(UserSecurityService.class);
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        var controller = controller(frontendService, sseEndpoint, sseService, userSecurityService);

        controller.subscribeToEvents(null, "client-123", null, null, request, response);

        verify(sseEndpoint).open(eq(request), eq(response), any(), any());
        verify(response, never()).setStatusCode(400);
    }

    @Test
    void subscribeToEventsReturnsBadRequestWhenClientIdIsMissingEverywhere() {
        var frontendService = mock(FrontendService.class);
        var sseEndpoint = mock(SseEndpoint.class);
        var sseService = mock(SseService.class);
        var userSecurityService = mock(UserSecurityService.class);
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        var controller = controller(frontendService, sseEndpoint, sseService, userSecurityService);

        controller.subscribeToEvents("  ", null, null, null, request, response);

        verify(response).setStatusCode(400);
        verify(response).setBody("Missing clientId");
        verifyNoInteractions(sseEndpoint);
    }

    @Test
    void subscribeToEventsWritesRenewedTokenCookiesBeforeOpeningSse() {
        var frontendService = mock(FrontendService.class);
        var sseEndpoint = mock(SseEndpoint.class);
        var sseService = mock(SseService.class);
        var userSecurityService = mock(UserSecurityService.class);
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        var controller = controller(frontendService, sseEndpoint, sseService, userSecurityService);

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
        verify(sseEndpoint).open(eq(request), eq(response), any(), any());
    }

    @Test
    void subscribeToEventsReturnsUnauthorizedWhenTokenCannotBeAuthenticated() {
        var frontendService = mock(FrontendService.class);
        var sseEndpoint = mock(SseEndpoint.class);
        var sseService = mock(SseService.class);
        var userSecurityService = mock(UserSecurityService.class);
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        var controller = controller(frontendService, sseEndpoint, sseService, userSecurityService);

        doThrow(new AuthenticationException()).when(userSecurityService).update(any(), any());

        controller.subscribeToEvents(null, "client-123", "expired-access-token", "invalid-refresh-token", request, response);

        verify(response).setStatusCode(401);
        verifyNoInteractions(sseEndpoint);
    }

    @Test
    @SuppressWarnings("unchecked")
    void subscribeToEventsRegistersEmitterWithXisSseService() {
        var frontendService = mock(FrontendService.class);
        var sseEndpoint = mock(SseEndpoint.class);
        var sseService = mock(SseService.class);
        var userSecurityService = mock(UserSecurityService.class);
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        var emitter = mock(one.xis.http.SseEmitter.class);
        var controller = controller(frontendService, sseEndpoint, sseService, userSecurityService);

        when(emitter.send(": connected\n\n")).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));
        doAnswer(invocation -> {
            Consumer<one.xis.http.SseEmitter> onOpen = invocation.getArgument(2);
            onOpen.accept(emitter);
            return null;
        }).when(sseEndpoint).open(eq(request), eq(response), any(), any());
        doAnswer(invocation -> {
            TokenStatus tokenStatus = invocation.getArgument(0);
            SecurityAttributes attributes = invocation.getArgument(1);
            attributes.setUserId("user-1");
            return null;
        }).when(userSecurityService).update(any(), any());

        controller.subscribeToEvents(null, "client-123", "access-token", "refresh-token", request, response);

        verify(sseService).registerEmitter("client-123", "user-1", emitter);
    }

    private MainController controller(FrontendService frontendService, SseEndpoint sseEndpoint, SseService sseService, UserSecurityService userSecurityService) {
        return new MainController(frontendService, sseEndpoint, sseService, userSecurityService, mock(ValidatorMessageResolver.class));
    }
}
