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

import static one.xis.js.JavascriptSource.*;
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
                + Javascript.getScript(EVENT_REGISTRY, CLASSES, FUNCTIONS) + "\n"

                + """
                var openedUrl = null;
                var openedOptions = null;
                class EventSource {
                    constructor(url, options) {
                        openedUrl = url;
                        openedOptions = options;
                        this.url = url;
                        this.onopen = null;
                        this.onmessage = null;
                        this.onerror = null;
                    }
                
                    close() {
                    }
                }
                
                var app = new Application();
                app.connectEventSources(null);
                openedUrl + '|' + openedOptions.withCredentials;
                """;

        var result = JSUtil.execute(script, Map.of(
                "window", window,
                "document", document,
                "localStorage", localStorage,
                "sessionStorage", new SessionStorage(),
                "console", new Console()
        )).asString();

        assertThat(result).isEqualTo("http://example.com/xis/events?clientId=test-client|true");
    }

    @Test
    void connectsSseToConfiguredRemoteHosts() throws ScriptException {
        var localStorage = new LocalStorage();
        localStorage.setItem("xis.clientId", "test-client");

        var location = new Location();
        location.setOrigin("http://shell.example.com");
        var window = new Window(location);
        var document = Document.of("<html><body></body></html>");

        var applicationScript = new Resources().getByPath("app.js").getContent();
        var script = applicationScript + "\n"
                + Javascript.getScript(EVENT_REGISTRY, CLASSES, FUNCTIONS) + "\n"

                + """
                var opened = [];
                class EventSource {
                    constructor(url, options) {
                        opened.push(url + '|' + options.withCredentials);
                        this.url = url;
                        this.readyState = 1;
                        this.onopen = null;
                        this.onmessage = null;
                        this.onerror = null;
                    }
                
                    close() {
                    }
                }
                
                var app = new Application();
                var config = new ClientConfig();
                config.pageAttributes = {
                    "/remote.html": { host: "http://remote.example.com" }
                };
                config.frontletAttributes = {
                    "RemoteFrontlet": { host: "http://frontlets.example.com" }
                };
                app.connectEventSources(config);
                opened.join(',');
                """;

        var result = JSUtil.execute(script, Map.of(
                "window", window,
                "document", document,
                "localStorage", localStorage,
                "sessionStorage", new SessionStorage(),
                "console", new Console()
        )).asString();

        assertThat(result).isEqualTo("http://shell.example.com/xis/events?clientId=test-client|true,"
                + "http://remote.example.com/xis/events?clientId=test-client|true,"
                + "http://frontlets.example.com/xis/events?clientId=test-client|true");
    }

    @Test
    void connectEventSourcesDoesNotBlockInitialPageLoad() throws ScriptException {
        var localStorage = new LocalStorage();
        localStorage.setItem("xis.clientId", "test-client");

        var location = new Location();
        location.setOrigin("http://example.com");
        var window = new Window(location);
        var document = Document.of("<html><body></body></html>");

        var applicationScript = new Resources().getByPath("app.js").getContent();
        var script = applicationScript + "\n"
                + Javascript.getScript(EVENT_REGISTRY, CLASSES, FUNCTIONS) + "\n"

                + """
                class EventSource {
                    static CONNECTING = 0;
                
                    constructor(url, options) {
                        this.url = url;
                        this.readyState = EventSource.CONNECTING;
                        this.onopen = null;
                        this.onmessage = null;
                        this.onerror = null;
                    }
                
                    close() {
                    }
                }
                
                var app = new Application();
                var config = new ClientConfig();
                app.connectEventSources(config) === config;
                """;

        var result = JSUtil.execute(script, Map.of(
                "window", window,
                "document", document,
                "localStorage", localStorage,
                "sessionStorage", new SessionStorage(),
                "console", new Console()
        )).asBoolean();

        assertThat(result).isTrue();
    }

    @Test
    void configuredLocalSseEndpointReusesInitialConnection() throws ScriptException {
        var localStorage = new LocalStorage();
        localStorage.setItem("xis.clientId", "test-client");

        var location = new Location();
        location.setOrigin("http://example.com");
        var window = new Window(location);
        var document = Document.of("<html><body></body></html>");

        var applicationScript = new Resources().getByPath("app.js").getContent();
        var script = applicationScript + "\n"
                + Javascript.getScript(EVENT_REGISTRY, CLASSES, FUNCTIONS) + "\n"

                + """
                var opened = [];
                class EventSource {
                    static CONNECTING = 0;
                    static CLOSED = 2;
                
                    constructor(url, options) {
                        opened.push(url + '|' + options.withCredentials);
                        this.url = url;
                        this.readyState = EventSource.CONNECTING;
                        this.onopen = null;
                        this.onmessage = null;
                        this.onerror = null;
                    }
                
                    close() {
                        this.readyState = EventSource.CLOSED;
                    }
                }
                
                var app = new Application();
                app.connectEventSources(null);
                app.connectEventSources(new ClientConfig());
                opened.join(',');
                """;

        var result = JSUtil.execute(script, Map.of(
                "window", window,
                "document", document,
                "localStorage", localStorage,
                "sessionStorage", new SessionStorage(),
                "console", new Console()
        )).asString();

        assertThat(result).isEqualTo("http://example.com/xis/events?clientId=test-client|true");
    }

    @Test
    void reconnectsClosedSseEndpointBeforeHttpRequest() throws ScriptException {
        var localStorage = new LocalStorage();
        localStorage.setItem("xis.clientId", "test-client");

        var location = new Location();
        location.setOrigin("http://example.com");
        var window = new Window(location);
        var document = Document.of("<html><body></body></html>");

        var applicationScript = new Resources().getByPath("app.js").getContent();
        var script = applicationScript + "\n"
                + Javascript.getScript(EVENT_REGISTRY, CLASSES, FUNCTIONS) + "\n"

                + """
                var opened = [];
                class EventSource {
                    static CLOSED = 2;
                
                    constructor(url, options) {
                        opened.push(url + '|' + options.withCredentials);
                        this.url = url;
                        this.readyState = 1;
                        this.onopen = null;
                        this.onmessage = null;
                        this.onerror = null;
                    }
                
                    close() {
                        this.readyState = EventSource.CLOSED;
                    }
                }
                
                class XMLHttpRequest {
                    open(method, uri, async) {
                        this.method = method;
                        this.uri = uri;
                        this.async = async;
                    }
                
                    setRequestHeader(name, value) {
                    }
                
                    send(payload) {
                    }
                }
                
                var app = new Application();
                app.connectEventSources(null);
                app.eventConnector.eventSource.readyState = EventSource.CLOSED;
                app.httpConnector.get('/xis/config', {});
                opened.length;
                """;

        var result = JSUtil.execute(script, Map.of(
                "window", window,
                "document", document,
                "localStorage", localStorage,
                "sessionStorage", new SessionStorage(),
                "console", new Console()
        )).asInt();

        assertThat(result).isEqualTo(2);
    }

    @Test
    void ensureEventConnectionDoesNotWaitForReconnectPromise() throws ScriptException {
        var localStorage = new LocalStorage();
        localStorage.setItem("xis.clientId", "test-client");

        var location = new Location();
        location.setOrigin("http://example.com");
        var window = new Window(location);
        var document = Document.of("<html><body></body></html>");

        var applicationScript = new Resources().getByPath("app.js").getContent();
        var script = applicationScript + "\n"
                + Javascript.getScript(EVENT_REGISTRY, CLASSES, FUNCTIONS) + "\n"

                + """
                var ensureCalls = 0;
                var app = {
                    eventConnector: {
                        ensureConnected: function() {
                            ensureCalls++;
                            return new Promise(resolve => {});
                        }
                    }
                };
                var connector = new HttpConnector('test-client');
                var promise = connector.ensureEventConnection();
                ensureCalls + '|' + (promise instanceof Promise);
                """;

        var result = JSUtil.execute(script, Map.of(
                "window", window,
                "document", document,
                "localStorage", localStorage,
                "sessionStorage", new SessionStorage(),
                "console", new Console()
        )).asString();

        assertThat(result).isEqualTo("1|true");
    }

    @Test
    void initialHttpRequestsDoNotAskSseConnection() throws ScriptException {
        var localStorage = new LocalStorage();
        localStorage.setItem("xis.clientId", "test-client");

        var location = new Location();
        location.setOrigin("http://example.com");
        var window = new Window(location);
        var document = Document.of("<html><body></body></html>");

        var applicationScript = new Resources().getByPath("app.js").getContent();
        var script = applicationScript + "\n"
                + Javascript.getScript(EVENT_REGISTRY, CLASSES, FUNCTIONS) + "\n"

                + """
                var ensureCalls = 0;
                var app = {
                    initializing: true,
                    eventConnector: {
                        ensureConnected: function() {
                            ensureCalls++;
                            return new Promise(resolve => {});
                        }
                    }
                };
                var connector = new HttpConnector('test-client');
                connector.ensureEventConnection();
                ensureCalls;
                """;

        var result = JSUtil.execute(script, Map.of(
                "window", window,
                "document", document,
                "localStorage", localStorage,
                "sessionStorage", new SessionStorage(),
                "console", new Console()
        )).asInt();

        assertThat(result).isZero();
    }

    @Test
    void reconnectsClosedSseEndpointAfterError() throws ScriptException {
        var localStorage = new LocalStorage();
        localStorage.setItem("xis.clientId", "test-client");

        var location = new Location();
        location.setOrigin("http://example.com");
        var window = new Window(location);
        var document = Document.of("<html><body></body></html>");

        var applicationScript = new Resources().getByPath("app.js").getContent();
        var script = applicationScript + "\n"
                + Javascript.getScript(EVENT_REGISTRY, CLASSES, FUNCTIONS) + "\n"

                + """
                var opened = [];
                var timers = {};
                var nextTimerId = 1;
                function setTimeout(callback, delay) {
                    var id = nextTimerId++;
                    timers[id] = callback;
                    return id;
                }
                function clearTimeout(id) {
                    delete timers[id];
                }
                function runTimers() {
                    var ids = Object.keys(timers);
                    for (var i = 0; i < ids.length; i++) {
                        var callback = timers[ids[i]];
                        delete timers[ids[i]];
                        callback();
                    }
                }
                
                class EventSource {
                    static CLOSED = 2;
                
                    constructor(url, options) {
                        opened.push(url + '|' + options.withCredentials);
                        this.url = url;
                        this.readyState = 1;
                        this.onopen = null;
                        this.onmessage = null;
                        this.onerror = null;
                    }
                
                    close() {
                        this.readyState = EventSource.CLOSED;
                    }
                }
                
                var app = new Application();
                app.connectEventSources(null);
                app.eventConnector.eventSource.readyState = EventSource.CLOSED;
                app.eventConnector.eventSource.onerror();
                runTimers();
                opened.length;
                """;

        var result = JSUtil.execute(script, Map.of(
                "window", window,
                "document", document,
                "localStorage", localStorage,
                "sessionStorage", new SessionStorage(),
                "console", new Console()
        )).asInt();

        assertThat(result).isEqualTo(2);
    }

    @Test
    void reconnectsConnectingSseEndpointImmediatelyAfterError() throws ScriptException {
        var localStorage = new LocalStorage();
        localStorage.setItem("xis.clientId", "test-client");

        var location = new Location();
        location.setOrigin("http://example.com");
        var window = new Window(location);
        var document = Document.of("<html><body></body></html>");

        var applicationScript = new Resources().getByPath("app.js").getContent();
        var script = applicationScript + "\n"
                + Javascript.getScript(EVENT_REGISTRY, CLASSES, FUNCTIONS) + "\n"

                + """
                var opened = [];
                var closed = 0;
                var delays = [];
                var timers = {};
                var nextTimerId = 1;
                function setTimeout(callback, delay) {
                    var id = nextTimerId++;
                    delays.push(delay);
                    timers[id] = callback;
                    return id;
                }
                function clearTimeout(id) {
                    delete timers[id];
                }
                function runTimers() {
                    var ids = Object.keys(timers);
                    for (var i = 0; i < ids.length; i++) {
                        var callback = timers[ids[i]];
                        delete timers[ids[i]];
                        callback();
                    }
                }
                
                class EventSource {
                    static CONNECTING = 0;
                    static OPEN = 1;
                    static CLOSED = 2;
                
                    constructor(url, options) {
                        opened.push(url + '|' + options.withCredentials);
                        this.url = url;
                        this.readyState = EventSource.CONNECTING;
                        this.onopen = null;
                        this.onmessage = null;
                        this.onerror = null;
                    }
                
                    close() {
                        closed++;
                        this.readyState = EventSource.CLOSED;
                    }
                }
                
                var app = new Application();
                app.connectEventSources(null);
                app.eventConnector.eventSource.onerror();
                runTimers();
                opened.length + '|' + closed + '|' + delays[1];
                """;

        var result = JSUtil.execute(script, Map.of(
                "window", window,
                "document", document,
                "localStorage", localStorage,
                "sessionStorage", new SessionStorage(),
                "console", new Console()
        )).asString();

        assertThat(result).isEqualTo("2|1|0");
    }

    @Test
    void pendingEventTtlLimitsReconnectBackoff() throws ScriptException {
        var localStorage = new LocalStorage();
        localStorage.setItem("xis.clientId", "test-client");

        var location = new Location();
        location.setOrigin("http://example.com");
        var window = new Window(location);
        var document = Document.of("<html><body></body></html>");

        var applicationScript = new Resources().getByPath("app.js").getContent();
        var script = applicationScript + "\n"
                + Javascript.getScript(EVENT_REGISTRY, CLASSES, FUNCTIONS) + "\n"

                + """
                var delays = [];
                function setTimeout(callback, delay) {
                    delays.push(delay);
                    return delays.length;
                }
                function clearTimeout(id) {
                }
                class EventSource {
                    static CONNECTING = 0;
                
                    constructor(url, options) {
                        this.url = url;
                        this.readyState = EventSource.CONNECTING;
                        this.onopen = null;
                        this.onmessage = null;
                        this.onerror = null;
                    }
                
                    close() {
                    }
                }
                
                var connector = new SseConnector('test-client');
                connector.setPendingEventTtlMs(2000);
                connector.reconnectAttempts['http://example.com/xis/events'] = 10;
                connector.scheduleReconnect('http://example.com/xis/events');
                connector.pendingEventTtlMs + '|' + delays[0];
                """;

        var result = JSUtil.execute(script, Map.of(
                "window", window,
                "document", document,
                "localStorage", localStorage,
                "sessionStorage", new SessionStorage(),
                "console", new Console()
        )).asString();

        assertThat(result).isEqualTo("2000|2000");
    }

    @Test
    void sseConnectionTimeoutAcceptsAlreadyOpenEventSource() throws ScriptException {
        var localStorage = new LocalStorage();
        localStorage.setItem("xis.clientId", "test-client");

        var location = new Location();
        location.setOrigin("http://example.com");
        var window = new Window(location);
        var document = Document.of("<html><body></body></html>");

        var applicationScript = new Resources().getByPath("app.js").getContent();
        var script = applicationScript + "\n"
                + Javascript.getScript(EVENT_REGISTRY, CLASSES, FUNCTIONS) + "\n"

                + """
                var timers = {};
                var nextTimerId = 1;
                function setTimeout(callback, delay) {
                    var id = nextTimerId++;
                    timers[id] = callback;
                    return id;
                }
                function clearTimeout(id) {
                    delete timers[id];
                }
                function runTimers() {
                    var ids = Object.keys(timers);
                    for (var i = 0; i < ids.length; i++) {
                        var callback = timers[ids[i]];
                        delete timers[ids[i]];
                        callback();
                    }
                }
                class EventSource {
                    static OPEN = 1;
                    static CLOSED = 2;
                
                    constructor(url, options) {
                        this.url = url;
                        this.readyState = EventSource.OPEN;
                        this.onopen = null;
                        this.onmessage = null;
                        this.onerror = null;
                    }
                
                    close() {
                        this.readyState = EventSource.CLOSED;
                    }
                }
                
                var connector = new SseConnector('test-client');
                connector.connectEndpoint('http://example.com/xis/events');
                runTimers();
                connector.connected + '|' + Object.keys(connector.reconnectTimers).length;
                """;

        var result = JSUtil.execute(script, Map.of(
                "window", window,
                "document", document,
                "localStorage", localStorage,
                "sessionStorage", new SessionStorage(),
                "console", new Console()
        )).asString();

        assertThat(result).isEqualTo("true|0");
    }

    @Test
    void closedSseEndpointRequestsThrottledAuthenticationCheck() throws ScriptException {
        var localStorage = new LocalStorage();
        localStorage.setItem("xis.clientId", "test-client");

        var location = new Location();
        location.setOrigin("http://example.com");
        var window = new Window(location);
        var document = Document.of("<html><body></body></html>");

        var applicationScript = new Resources().getByPath("app.js").getContent();
        var script = applicationScript + "\n"
                + Javascript.getScript(EVENT_REGISTRY, CLASSES, FUNCTIONS) + "\n"

                + """
                var refreshCount = 0;
                var timers = {};
                var nextTimerId = 1;
                function setTimeout(callback, delay) {
                    var id = nextTimerId++;
                    timers[id] = callback;
                    return id;
                }
                function clearTimeout(id) {
                    delete timers[id];
                }
                class EventSource {
                    static CLOSED = 2;
                
                    constructor(url, options) {
                        this.url = url;
                        this.readyState = 1;
                        this.onopen = null;
                        this.onmessage = null;
                        this.onerror = null;
                    }
                
                    close() {
                        this.readyState = EventSource.CLOSED;
                    }
                }
                
                var app = new Application();
                app.connectEventSources(null);
                app.pageController = {
                    resolvedURL: {},
                    refreshCurrentPage: function() {
                        refreshCount++;
                        return Promise.resolve();
                    }
                };
                app.eventConnector.eventSource.readyState = EventSource.CLOSED;
                app.eventConnector.eventSource.onerror();
                app.eventConnector.eventSource.onerror();
                refreshCount;
                """;

        var result = JSUtil.execute(script, Map.of(
                "window", window,
                "document", document,
                "localStorage", localStorage,
                "sessionStorage", new SessionStorage(),
                "console", new Console()
        )).asInt();

        assertThat(result).isEqualTo(1);
    }

    @Test
    void browserNavigationClosesSseWithoutReconnectOrAuthenticationCheck() throws ScriptException {
        var localStorage = new LocalStorage();
        localStorage.setItem("xis.clientId", "test-client");

        var location = new Location();
        location.setOrigin("http://example.com");
        var window = new Window(location);
        var document = Document.of("<html><body></body></html>");

        var applicationScript = new Resources().getByPath("app.js").getContent();
        var script = applicationScript + "\n"
                + Javascript.getScript(EVENT_REGISTRY, CLASSES, FUNCTIONS) + "\n"

                + """
                var opened = [];
                var refreshCount = 0;
                var timers = {};
                var nextTimerId = 1;
                function setTimeout(callback, delay) {
                    var id = nextTimerId++;
                    timers[id] = callback;
                    return id;
                }
                function clearTimeout(id) {
                    delete timers[id];
                }
                function runTimers() {
                    var ids = Object.keys(timers);
                    for (var i = 0; i < ids.length; i++) {
                        var callback = timers[ids[i]];
                        delete timers[ids[i]];
                        callback();
                    }
                }
                
                class EventSource {
                    static CLOSED = 2;
                
                    constructor(url, options) {
                        opened.push(url + '|' + options.withCredentials);
                        this.url = url;
                        this.readyState = 1;
                        this.onopen = null;
                        this.onmessage = null;
                        this.onerror = null;
                    }
                
                    close() {
                        this.readyState = EventSource.CLOSED;
                    }
                }
                
                var app = new Application();
                app.connectEventSources(null);
                app.pageController = {
                    resolvedURL: {},
                    refreshCurrentPage: function() {
                        refreshCount++;
                        return Promise.resolve();
                    }
                };
                var oldEventSource = app.eventConnector.eventSource;
                app.prepareForBrowserNavigation();
                oldEventSource.onerror();
                runTimers();
                opened.length + '|' + refreshCount + '|' + Object.keys(app.eventConnector.eventSources).length;
                """;

        var result = JSUtil.execute(script, Map.of(
                "window", window,
                "document", document,
                "localStorage", localStorage,
                "sessionStorage", new SessionStorage(),
                "console", new Console()
        )).asString();

        assertThat(result).isEqualTo("1|0|0");
    }
}
