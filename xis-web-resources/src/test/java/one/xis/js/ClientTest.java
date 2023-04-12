package one.xis.js;

import one.xis.test.js.JSUtil;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.script.ScriptException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientTest {

    private String script;
    private HttpClient httpClient;
    private Promise promise;

    @BeforeAll
    void init() {
        httpClient = Mockito.mock(HttpClient.class);
        promise = Mockito.mock(Promise.class);
        when(httpClient.get(anyString(), any())).thenReturn(promise);
        var clientJs = IOUtils.getResourceAsString("js/Client.js");
        var instantiation = "var client = new Client(httpClient);";
        script = clientJs + instantiation;
    }

    @Test
    void loadConfig() throws ScriptException {
        var compiledScript = JSUtil.compile(script + "client.loadConfig();", Map.of("httpClient", httpClient));
        System.out.println(compiledScript.eval());

        verify(httpClient).get(eq("/xis/config"), any());
        verify(promise).then(any());
    }

    @Test
    void loadPageHead() throws ScriptException {
        var compiledScript = JSUtil.compile(script + "client.loadPageHead('x.html');", Map.of("httpClient", httpClient));
        compiledScript.eval();

        var captor = ArgumentCaptor.forClass(Map.class);
        verify(httpClient, times(1)).get(eq("/xis/page/head"), (Map<String, String>) captor.capture());

        assertThat(captor.getValue().get("uri")).isEqualTo("x.html");
    }

    @Test
    void loadPageBody() throws ScriptException {
        var compiledScript = JSUtil.compile(script + "client.loadPageBody('x.html');", Map.of("httpClient", httpClient));
        compiledScript.eval();

        var captor = ArgumentCaptor.forClass(Map.class);
        verify(httpClient, times(1)).get(eq("/xis/page/body"), (Map<String, String>) captor.capture());

        assertThat(captor.getValue().get("uri")).isEqualTo("x.html");

    }


    @Test
    void loadPageBodyAttributes() throws ScriptException {
        ;
    }


    @Test
    void loadWidget() throws ScriptException {

    }

    @Test
    void loadPageData() throws ScriptException {

    }

    @Test
    void loadWidgetData() throws ScriptException {

    }

    @Test
    void widgetAction() throws ScriptException {

    }

    @Test
    void pageAction() {

    }


}
