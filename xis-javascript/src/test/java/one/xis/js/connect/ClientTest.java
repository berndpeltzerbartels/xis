package one.xis.js.connect;

import one.xis.js.Javascript;
import one.xis.js.Promise;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.script.ScriptException;
import java.util.Map;

import static one.xis.js.JavascriptSource.CLASSES;
import static one.xis.js.JavascriptSource.FUNCTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientTest {

    private String script;
    private HttpConnector httpConnector;
    private Promise promise;

    @BeforeAll
    void init() {
        httpConnector = Mockito.mock(HttpConnector.class);
        promise = Mockito.mock(Promise.class);
        when(httpConnector.get(any(), any())).thenReturn(promise);
        var clientJs = Javascript.getScript(CLASSES, FUNCTIONS);
        var instantiation = "var client = new HttpClient(httpConnector);";
        script = clientJs + instantiation;
    }

    @Test
    @SuppressWarnings("unchecked")
    void loadPageHead() throws ScriptException {
        JSUtil.execute(script + "client.loadPageHead('x.html');", Map.of("httpConnector", httpConnector));

        var captor = ArgumentCaptor.forClass(Map.class);
        verify(httpConnector, times(1)).get(eq("/xis/page/head"), captor.capture());

        assertThat(captor.getValue().get("uri")).isEqualTo("x.html");
    }

    @Test
    void loadPageBody() throws ScriptException {
        JSUtil.execute(script + "client.loadPageBody('x.html');", Map.of("httpConnector", httpConnector));

        var captor = ArgumentCaptor.forClass(Map.class);
        verify(httpConnector, times(1)).get(eq("/xis/page/body"), (Map<String, String>) captor.capture());

        assertThat(captor.getValue().get("uri")).isEqualTo("x.html");

    }


    @Test
    void loadPageBodyAttributes() throws ScriptException {
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
