package one.xis.test.js.api;

import one.xis.test.JavaScript;
import one.xis.test.mocks.Console;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;


@Disabled
class StringTokenParserTest {

    @Test
    void stringToken() throws ScriptException {
        var console = new Console(System.out);
        var script = JavaScript.builder()
                .withApi()
                .withScript("var result = {};")
                .withScript("var text = \"'abc123'\";")
                .withScript("console.log(text);")
                .withScript("new StringParameterTokenParser(text).tryParse();")
                .withBinding("console", console)
                .build();

        var result = script.runForMap();
        assertThat(result).containsEntry("string", "abc123");
    }
}
