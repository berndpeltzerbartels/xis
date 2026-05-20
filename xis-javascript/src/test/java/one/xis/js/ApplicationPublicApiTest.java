package one.xis.js;

import one.xis.resource.Resources;
import one.xis.test.dom.Location;
import one.xis.test.dom.Window;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPublicApiTest {

    @Test
    void mainPublishesSmallPublicApiWithoutExposingApplicationInstance() throws ScriptException {
        var window = new Window(new Location());
        var mainScript = new Resources().getByPath("main.js").getContent();
        var classesScript = new Resources().getByPath("classes.js").getContent();
        var script = """
                var submitted = null;
                var opened = null;
                var started = false;
                var eventListenerRegistry = { listeners: [] };

                class Application {
                    constructor() {
                        this.eventPublisher = {};
                        this.history = { onPopState(event) {} };
                        this.eventConnector = {
                            closed: false,
                            isConnected() {
                                return !this.closed;
                            },
                            close() {
                                this.closed = true;
                            }
                        };
                        this.pageController = {
                            displayPageForUrl(url) {
                                opened = url;
                                return 'opened:' + url;
                            }
                        };
                    }

                    start() {
                        started = true;
                    }

                    submitForm(formId, action) {
                        submitted = formId + ':' + action;
                        return submitted;
                    }
                }
                """ + mainScript + classesScript + """

                window.XIS.addElFunction('initials', function(value) {
                    return String(value).substring(0, 1).toUpperCase();
                });
                var expression = new ExpressionParser(elFunctions).parse("initials(name)");
                var customFunctionResult = expression.evaluate(new Data({name: 'mara'}));

                main();

                [
                    started,
                    typeof window.app === 'undefined',
                    !!window.XIS,
                    Object.isFrozen(window.XIS),
                    window.XIS.addElFunction !== undefined,
                    window.XIS.submitForm('moveForm', 'doMove'),
                    window.XIS.openPage('/customers.html'),
                    window.XIS.isEventStreamConnected(),
                    window.XIS.closeEventStreams(),
                    window.XIS.isEventStreamConnected(),
                    customFunctionResult,
                    submitted,
                    opened
                ].join('|');
                """;

        var result = JSUtil.execute(script, Map.of("window", window)).asString();

        assertThat(result).isEqualTo("true|true|true|true|true|moveForm:doMove|opened:/customers.html|true||false|M|moveForm:doMove|/customers.html");
    }
}
