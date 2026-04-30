package one.xis.server;

import one.xis.http.HttpRequest;
import one.xis.http.HttpResponse;
import one.xis.http.SseEndpoint;
import one.xis.auth.token.UserSecurityService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class MainControllerTest {

    @Test
    void subscribeToEventsUsesQueryParameterWhenHeaderIsMissing() {
        var frontendService = mock(FrontendService.class);
        var sseEndpoint = mock(SseEndpoint.class);
        var userSecurityService = mock(UserSecurityService.class);
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        var controller = new MainController(frontendService, sseEndpoint, userSecurityService);

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
        var controller = new MainController(frontendService, sseEndpoint, userSecurityService);

        controller.subscribeToEvents("  ", null, null, null, request, response);

        verify(response).setStatusCode(400);
        verify(response).setBody("Missing clientId");
        verifyNoInteractions(sseEndpoint);
    }
}
