package one.xis.http;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublicResourceHandlerTest {

    @Test
    void rootPathIsNotHandledAsPublicResourceDirectory() {
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        when(request.getPath()).thenReturn("/");

        boolean handled = new PublicResourceHandler(List.of("/test-public")).handle(request, response);

        assertThat(handled).isFalse();
        verify(response, never()).setStatusCode(200);
    }

    @Test
    void publicFileIsStillHandled() {
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        when(request.getPath()).thenReturn("/probe.txt");

        boolean handled = new PublicResourceHandler(List.of("/test-public")).handle(request, response);

        assertThat(handled).isTrue();
        verify(response).setStatusCode(200);
        verify(response).setBody("probe\n".getBytes());
    }
}
