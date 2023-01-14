package one.xis.test.js.api;

import one.xis.test.JavaScript;
import one.xis.test.mocks.Console;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SignatureTokenTest {

    @Test
    void emptySignature() throws ScriptException {
        var console = new Console();
        var script = JavaScript.builder()
                .withApi()
                .withScript("var result = {};")
                .withScript("var token = new SignatureToken('()');")
                .withScript("result['success'] = token.tryParse();")
                .withScript("result['token'] = token;")
                .withScript("result")
                .withBinding("console", console)
                .build();

        Map<String, Object> result = null;
        try {
            result = script.runForMap();
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        var lines = console.getInfoLog();
        assertThat(result).containsEntry("success", true);
        var token = (Map<String, Object>) result.get("token");
        /*
        assertThat(token).containsEntry("src", "abc()");
        assertThat(token).containsEntry("end", 4);
        assertThat(token).containsEntry("functionName", "abc");

         */

    }
}
