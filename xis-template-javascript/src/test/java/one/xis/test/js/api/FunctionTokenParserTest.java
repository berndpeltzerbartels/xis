package one.xis.test.js.api;

import one.xis.test.JavaScript;
import one.xis.test.mocks.Console;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class FunctionTokenParserTest {


    @Test
    @SuppressWarnings("unchecked")
    void simpleFunction() throws ScriptException {
        var console = new Console();
        var script = JavaScript.builder()
                .withApi()
                .withScript("var result = {};")
                .withScript("var parser = new FunctionTokenParser('abc()');")
                .withScript("parser.tryParse();")
                .withBinding("console", console)
                .build();
        var result = script.runForMap();
        var lines = console.getInfoLog();
        assertThat(result).containsEntry("functionName", "abc");
    }
}
