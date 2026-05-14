package one.xis.js;

import one.xis.resource.Resources;
import one.xis.test.dom.Document;
import one.xis.test.dom.Location;
import one.xis.test.dom.NodeConstants;
import one.xis.test.dom.Window;
import one.xis.test.js.Console;
import one.xis.test.js.JSUtil;
import one.xis.test.js.LocalStorage;
import one.xis.test.js.SessionStorage;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Map;

import static one.xis.js.JavascriptSource.*;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationSubmitFormTest {

    @Test
    void submitFormSubmitsXisFormByHtmlId() throws ScriptException {
        var localStorage = new LocalStorage();
        localStorage.setItem("xis.clientId", "test-client");

        var location = new Location();
        location.setOrigin("http://example.com");
        var window = new Window(location);
        var document = Document.of("""
                <html>
                    <body>
                        <form id="moveForm" xis:binding="move"></form>
                    </body>
                </html>
                """);

        var applicationScript = new Resources().getByPath("app.js").getContent();
        var script = applicationScript + "\n"
                + Javascript.getScript(EVENT_REGISTRY, CLASSES, FUNCTIONS) + "\n"
                + """
                class EventSource {
                    constructor(url, options) {
                        this.url = url;
                        this.options = options;
                    }
                
                    close() {
                    }
                }
                
                var app = new Application();
                var submittedAction = null;
                app.tagHandlers = {
                    getHandler(form) {
                        return {
                            type: 'form-handler',
                            submit(action) {
                                submittedAction = action;
                            }
                        };
                    }
                };
                
                app.submitForm('moveForm', 'doMove');
                submittedAction;
                """;

        var result = JSUtil.execute(script, Map.of(
                "window", window,
                "document", document,
                "localStorage", localStorage,
                "sessionStorage", new SessionStorage(),
                "Node", new NodeConstants(),
                "console", new Console()
        )).asString();

        assertThat(result).isEqualTo("doMove");
    }
}
