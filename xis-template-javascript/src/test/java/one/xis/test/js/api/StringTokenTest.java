package one.xis.test.js.api;

import one.xis.test.JavaScript;
import one.xis.test.mocks.Console;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

class StringTokenTest {


    @Test
    void stringToken() throws ScriptException {
        var console = new Console(System.out);
        var script = JavaScript.builder()
                .withApi()
                .withScript("var result = {};")
                .withScript("var text = \"'abc123'\";")
                .withScript("console.log(text);")
                .withScript("var token = new StringToken(text);")
                .withScript("result['success'] = token.tryParse();")
                .withScript("result['string'] = token.string;")
                .withScript("result['endIndex'] = token.end;")
                .withScript("result")
                .withBinding("console", console)
                .build();

        var result = script.runForMap();
        assertThat(result).containsEntry("success", true);
        assertThat(result).containsEntry("string", "abc123");
        assertThat(result).containsEntry("endIndex", 7); // 2 for quotation marks
    }
}
