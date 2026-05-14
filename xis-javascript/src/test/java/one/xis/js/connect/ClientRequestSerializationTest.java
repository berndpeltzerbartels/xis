package one.xis.js.connect;

import one.xis.js.Javascript;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static one.xis.js.JavascriptSource.CLASSES;
import static org.assertj.core.api.Assertions.assertThat;

class ClientRequestSerializationTest {

    @Test
    void omitsEmptyRequestFieldsButKeepsNullMapEntries() throws ScriptException {
        var script = Javascript.getScript(CLASSES) + """
                var request = new ClientRequest();
                request.clientId = 'client-1';
                request.pageId = '/index.html';
                request.type = 'page';
                request.action = null;
                request.formData = {};
                request.actionParameters = { id: '42' };
                request.sessionStorageData = { selectedCustomer: null };
                JSON.stringify(request);
                """;

        var json = JSUtil.execute(script).asString();

        assertThat(json).contains("\"clientId\":\"client-1\"");
        assertThat(json).contains("\"pageId\":\"/index.html\"");
        assertThat(json).contains("\"type\":\"page\"");
        assertThat(json).contains("\"actionParameters\":{\"id\":\"42\"}");
        assertThat(json).contains("\"sessionStorageData\":{\"selectedCustomer\":null}");
        assertThat(json).doesNotContain("formData");
        assertThat(json).doesNotContain("action\":");
        assertThat(json).doesNotContain("frontletParameters");
    }

    @Test
    void frontletRequestIncludesCurrentPageContext() throws ScriptException {
        var script = Javascript.getScript(CLASSES) + """
                function timeZone() { return 'Europe/Berlin'; }
                var client = new Client('client-1');
                client.sessionStorageDataFrontlet = function() { return {}; };
                client.localStorageDataFrontlet = function() { return {}; };
                client.clientStorageDataFrontlet = function() { return {}; };
                client.globalVariableDataFrontlet = function() { return {}; };
                var frontletInstance = { frontlet: { id: 'ReminderFrontlet' } };
                var frontletState = {
                    resolvedURL: {
                        normalizedPath: '/customers.html',
                        url: '/customers.html?selected=42',
                        urlParameters: { selected: '42' },
                        pathVariablesAsMap: function() { return {}; }
                    },
                    frontletParameters: {}
                };
                var request = client.createFrontletRequest(frontletInstance, frontletState, null, null, null);
                JSON.stringify(request);
                """;

        var json = JSUtil.execute(script).asString();

        assertThat(json).contains("\"pageId\":\"/customers.html\"");
        assertThat(json).contains("\"pageUrl\":\"/customers.html?selected=42\"");
        assertThat(json).contains("\"frontletId\":\"ReminderFrontlet\"");
    }

    @Test
    void validatorMessagesDefaultsToArrayForGlobalMessages() throws ScriptException {
        var script = Javascript.getScript(CLASSES) + """
                var empty = new ValidatorMessages();
                var compact = new ValidatorMessages({});
                JSON.stringify({
                    emptyGlobalIsArray: Array.isArray(empty.globalMessages),
                    compactGlobalIsArray: Array.isArray(compact.globalMessages),
                    emptyMessagesKeys: Object.keys(empty.messages).length,
                    compactIsEmpty: compact.isEmpty()
                });
                """;

        var json = JSUtil.execute(script).asString();

        assertThat(json).contains("\"emptyGlobalIsArray\":true");
        assertThat(json).contains("\"compactGlobalIsArray\":true");
        assertThat(json).contains("\"emptyMessagesKeys\":0");
        assertThat(json).contains("\"compactIsEmpty\":true");
    }
}
