package one.xis.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.mockito.Mockito.*;

class ResponseWriterCookieTest {

    private final ResponseWriter responseWriter = new ResponseWriter(new Gson());

    @Test
    void secureCookieFlagIsRemovedForPlainHttpRequests() throws NoSuchMethodException {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);

        responseWriter.write(secureCookieResponse(), method(), request, response);

        verify(response).addHeader(eq("SET-COOKIE"), argThat(cookie ->
                cookie.contains("access_token=abc") && !cookie.contains("Secure")));
    }

    @Test
    void secureCookieFlagIsKeptForHttpsRequests() throws NoSuchMethodException {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        when(request.isSecure()).thenReturn(true);

        responseWriter.write(secureCookieResponse(), method(), request, response);

        verify(response).addHeader(eq("SET-COOKIE"), argThat(cookie ->
                cookie.contains("access_token=abc") && cookie.contains("Secure")));
    }

    @Test
    void headRequestsDoNotWriteResponseBody() throws NoSuchMethodException {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        when(request.getHttpMethod()).thenReturn(HttpMethod.HEAD);
        when(request.getSuffix()).thenReturn("");
        when(response.getStatusCode()).thenReturn(null);

        responseWriter.write("metadata", method(), request, response);

        verify(response).setStatusCode(200);
        verify(response, never()).setBody(any(byte[].class));
        verify(response, never()).setBody(anyString());
    }

    private ResponseEntity<?> secureCookieResponse() {
        return ResponseEntity.noContent()
                .addSecureCookie("access_token", "abc", Duration.ofMinutes(5));
    }

    private java.lang.reflect.Method method() throws NoSuchMethodException {
        return DummyController.class.getDeclaredMethod("callback");
    }

    private static class DummyController {
        @Get("/callback")
        ResponseEntity<?> callback() {
            return ResponseEntity.noContent();
        }
    }
}
