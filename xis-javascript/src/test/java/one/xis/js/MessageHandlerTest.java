package one.xis.js;

import one.xis.test.js.JSUtil;
import one.xis.test.dom.Document;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Map;

import static one.xis.js.JavascriptSource.CLASSES;
import static org.assertj.core.api.Assertions.assertThat;

class MessageHandlerTest {

    @Test
    void serverErrorsAreReportedAsErrorToasts() throws ScriptException {
        var script = Javascript.getScript(CLASSES) + """
                var shown = [];
                var handler = Object.create(MessageHandler.prototype);
                handler.showToast = function(message, level) {
                    shown.push({ message: message, level: level });
                };
                handler.reportServerError('Internal server error');
                JSON.stringify(shown);
                """;

        var json = JSUtil.execute(script).asString();

        assertThat(json).contains("\"message\":\"Internal server error\"");
        assertThat(json).contains("\"level\":\"error\"");
    }

    @Test
    void duplicateActiveToastsAreRenderedOnlyOnce() throws ScriptException {
        var document = Document.of("<html><body></body></html>");
        var script = Javascript.getScript(CLASSES) + """
                var handler = new MessageHandler();
                handler.showToast('connection problem', 'error');
                handler.showToast('connection problem', 'error');
                document.getElementById('xis-toast-container').childNodes.length;
                """;

        var result = JSUtil.execute(script, Map.of(
                "document", document,
                "setTimeout", (java.util.function.BiFunction<Object, Object, Integer>) (callback, delay) -> 1
        )).asInt();

        assertThat(result).isEqualTo(1);
    }

    @Test
    void errorMessagesFallbackToToastWhenNoMessageContainerExists() throws ScriptException {
        var document = Document.of("<html><body></body></html>");
        var script = Javascript.getScript(CLASSES) + """
                var handler = new MessageHandler();
                handler.addErrorMessage('Error during HTTP request');
                var container = document.getElementById('xis-toast-container');
                container.childNodes.length + ':' + container.firstChild.textContent;
                """;

        var result = JSUtil.execute(script, Map.of(
                "document", document,
                "setTimeout", (java.util.function.BiFunction<Object, Object, Integer>) (callback, delay) -> 1
        )).asString();

        assertThat(result).isEqualTo("1:Error during HTTP request");
    }
}
