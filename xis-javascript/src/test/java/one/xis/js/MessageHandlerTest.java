package one.xis.js;

import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

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
}
