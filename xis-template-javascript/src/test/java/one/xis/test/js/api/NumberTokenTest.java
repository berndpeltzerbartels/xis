package one.xis.test.js.api;

import one.xis.test.JavaScript;
import one.xis.test.mocks.Console;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

class NumberTokenTest {

    @Test
    void integerToken() throws ScriptException {
        var console = new Console();
        var script = JavaScript.builder()
                .withApi()
                .withScript("var result = {};")
                .withScript("var token = new NumberToken('123');")
                .withScript("result['success'] = token.tryParse();")
                .withScript("result['number'] = token.number;")
                .withScript("result['length'] = token.length;")
                .withScript("result")
                .withBinding("console", console)
                .build();

        var result = script.runForMap();
        assertThat(result).containsEntry("success", true);
        assertThat(result).containsEntry("number", 123);
        assertThat(result).containsEntry("length", 3);
    }

    @Test
    void negativIntegerToken() throws ScriptException {
        var console = new Console();
        var script = JavaScript.builder()
                .withApi()
                .withScript("var result = {};")
                .withScript("var token = new NumberToken('-1');")
                .withScript("result['success'] = token.tryParse();")
                .withScript("result['number'] = token.number;")
                .withScript("result['length'] = token.length;")
                .withScript("result")
                .withBinding("console", console)
                .build();

        var result = script.runForMap();
        assertThat(result).containsEntry("success", true);
        assertThat(result).containsEntry("number", -1);
        assertThat(result).containsEntry("length", 2);
    }

    @Test
    void decimalToken() throws ScriptException {
        var console = new Console();
        var script = JavaScript.builder()
                .withApi()
                .withScript("var result = {};")
                .withScript("var token = new NumberToken('1.23');")
                .withScript("result['success'] = token.tryParse();")
                .withScript("result['number'] = token.number;")
                .withScript("result['length'] = token.length;")
                .withScript("result")
                .withBinding("console", console)
                .build();

        var result = script.runForMap();
        assertThat(result).containsEntry("success", true);
        assertThat(result).containsEntry("number", 1.23);
        assertThat(result).containsEntry("length", 4);
    }

    @Test
    void negativDecimalToken() throws ScriptException {
        var console = new Console();
        var script = JavaScript.builder()
                .withApi()
                .withScript("var result = {};")
                .withScript("var token = new NumberToken('-1.2');")
                .withScript("result['success'] = token.tryParse();")
                .withScript("result['number'] = token.number;")
                .withScript("result['endIndex'] = token.end;")
                .withScript("result")
                .withBinding("console", console)
                .build();

        var result = script.runForMap();
        assertThat(result).containsEntry("success", true);
        assertThat(result).containsEntry("number", -1.2);
        assertThat(result).containsEntry("endIndex", 3);
    }
}
