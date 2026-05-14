package one.xis.js.store;

import one.xis.js.Javascript;
import one.xis.test.js.JSUtil;
import one.xis.test.js.LocalStorage;
import one.xis.test.js.SessionStorage;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Map;

import static one.xis.js.JavascriptSource.CLASSES;
import static org.assertj.core.api.Assertions.assertThat;

class StoreUpdaterTest {

    @Test
    void skipsMissingStorageBlocksInCompactResponses() throws ScriptException {
        var script = Javascript.getScript(CLASSES) + """
                var window = { addEventListener: function() {} };
                var publishedEvents = [];
                var eventPublisher = { publish: function(eventKey) { publishedEvents.push(eventKey); } };
                var app = {
                    localStorage: new LocalStore(eventPublisher),
                    sessionStorage: new SessionStore(eventPublisher),
                    clientStorage: new ClientStore(eventPublisher)
                };
                var updater = new StoreUpdater();
                updater.updateStores({ data: {} });
                JSON.stringify({ events: publishedEvents.length });
                """;

        var json = JSUtil.execute(script, Map.of(
                "localStorage", new LocalStorage(),
                "sessionStorage", new SessionStorage()
        )).asString();

        assertThat(json).contains("\"events\":0");
    }
}
