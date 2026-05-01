package one.xis.js;

import one.xis.resource.Resources;
import one.xis.test.dom.Document;
import one.xis.test.dom.Location;
import one.xis.test.dom.Window;
import one.xis.test.js.Console;
import one.xis.test.js.JSUtil;
import one.xis.test.js.LocalStorage;
import one.xis.test.js.SessionStorage;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Map;

import static one.xis.js.JavascriptSource.CLASSES;
import static one.xis.js.JavascriptSource.FUNCTIONS;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationSseTest {

    @Test
    void usesSseEndpointWithClientIdQueryParameter() throws ScriptException {
        var localStorage = new LocalStorage();
        localStorage.setItem("xis.clientId", "test-client");

        var location = new Location();
        location.setOrigin("http://example.com");
        var window = new Window(location);
        var document = Document.of("<html><body></body></html>");

        var applicationScript = new Resources().getByPath("app.js").getContent();
        var script = applicationScript + "\n"
                + Javascript.getScript(CLASSES, FUNCTIONS) + "\n"
                + """
                var openedUrl = null;
                class EventSource {
                    constructor(url) {
                        openedUrl = url;
                        this.url = url;
                        this.onopen = null;
                        this.onmessage = null;
                        this.onerror = null;
                    }

                    close() {
                    }
                }

                var app = new Application();
                openedUrl;
                """;

        var result = JSUtil.execute(script, Map.of(
                "window", window,
                "document", document,
                "localStorage", localStorage,
                "sessionStorage", new SessionStorage(),
                "console", new Console()
        )).asString();

        assertThat(result).isEqualTo("http://example.com/xis/events?clientId=test-client");
    }
}
