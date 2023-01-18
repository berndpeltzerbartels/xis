package one.xis.test.js.api;

import one.xis.test.JavaScript;
import one.xis.test.mocks.Console;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

class SignatureTokenTest {

    @Test
    void signatureWithoutParameter() throws ScriptException {
        var console = new Console();
        var script = JavaScript.builder()
                .withApi()
                .withScript("var result = {};")
                .withScript("var token = new SignatureToken('()');")
                .withScript("result['success'] = token.tryParse();")
                .withScript("result['token'] = token;")
                .withScript("result['parameterCount'] = token.parameterTokens.length;")
                .withScript("result")
                .withBinding("console", console)
                .build();

        var result = script.runForMap();
        assertThat(result).containsEntry("success", true);
        assertThat(result).containsEntry("parameterCount", 0);
    }


    @Test
    void signatureWithParameter() throws ScriptException {
        var console = new Console(System.out);
        var script = JavaScript.builder()
                .withApi()
                .withScript("var result = {};")
                .withScript("var token = new SignatureToken('(123)');")
                .withScript("result['success'] = token.tryParse();")
                .withScript("result['token'] = token;")
                .withScript("result['numberParam'] = token.parameterTokens[0].number;")
                .withScript("result")
                .withBinding("console", console)
                .build();

        var result = script.runForMap();
        assertThat(result).containsEntry("success", true);
        assertThat(result).containsEntry("numberParam", 123);
    }


    @Test
    void signatureNumAndStringParameter() throws ScriptException {
        var console = new Console(System.out);
        var script = JavaScript.builder()
                .withApi()
                .withScript("var result = {};")
                .withScript("var token = new SignatureToken('(123,\\'bla\\')');")
                .withScript("result['success'] = token.tryParse();")
                .withScript("result['token'] = token;")
                .withScript("result['numberParam'] = token.parameterTokens[0].number;")
                .withScript("result['stringParam'] = token.parameterTokens[1].string;")
                .withScript("result")
                .withBinding("console", console)
                .build();

        var result = script.runForMap();
        assertThat(result).containsEntry("success", true);
        assertThat(result).containsEntry("numberParam", 123);
        assertThat(result).containsEntry("stringParam", "bla");
    }


    @Test
    void signatureNumAndStringParameterAndWhitespaces() throws ScriptException {
        var console = new Console(System.out);
        var script = JavaScript.builder()
                .withApi()
                .withScript("var result = {};")
                .withScript("var token = new SignatureToken('( 123 , \\'bla\\' )');")
                .withScript("result['success'] = token.tryParse();")
                .withScript("result['token'] = token;")
                .withScript("result['numberParam'] = token.parameterTokens[0].number;")
                .withScript("result['stringParam'] = token.parameterTokens[1].string;")
                .withScript("result")
                .withBinding("console", console)
                .build();

        var result = script.runForMap();
        assertThat(result).containsEntry("success", true);
        assertThat(result).containsEntry("numberParam", 123);
        assertThat(result).containsEntry("stringParam", "bla");
    }


    @Test
    void signatureFunctionInFunctionParameter() throws ScriptException {
        var console = new Console(System.out);
        var script = JavaScript.builder()
                .withApi()
                .withScript("var result = {};")
                .withScript("var token = new SignatureToken('(x1(x2(123)))');")
                .withScript("result['success'] = token.tryParse();")
                .withScript("var functionToken1 = token.parameterTokens[0];")
                .withScript("var functionToken2 = functionToken1.signatureToken.parameterTokens[0];")
                .withScript("var numberToken = functionToken2.signatureToken.parameterTokens[0]")
                .withScript("result['functionName1'] = functionToken1.functionName;")
                .withScript("result['functionName2'] = functionToken2.functionName;")
                .withScript("result['number'] = numberToken.number;")
                .withScript("result")
                .withBinding("console", console)
                .build();

        var result = script.runForMap();
        assertThat(result).containsEntry("success", true);
        assertThat(result).containsEntry("functionName1", "x1");
        assertThat(result).containsEntry("functionName2", "x2");
        assertThat(result).containsEntry("number", 123);
    }

    @Test
    void signatureFunctionInFunctionParameterAndWhitespaces() throws ScriptException {
        var console = new Console(System.out);
        var script = JavaScript.builder()
                .withApi()
                .withScript("var result = {};")
                .withScript("var token = new SignatureToken('( x1( x2( 123 ) ) )');")
                .withScript("result['success'] = token.tryParse();")
                .withScript("var functionToken1 = token.parameterTokens[0];")
                .withScript("var functionToken2 = functionToken1.signatureToken.parameterTokens[0];")
                .withScript("var numberToken = functionToken2.signatureToken.parameterTokens[0]")
                .withScript("result['functionName1'] = functionToken1.functionName;")
                .withScript("result['functionName2'] = functionToken2.functionName;")
                .withScript("result['number'] = numberToken.number;")
                .withScript("result")
                .withBinding("console", console)
                .build();

        var result = script.runForMap();
        assertThat(result).containsEntry("success", true);
        assertThat(result).containsEntry("functionName1", "x1");
        assertThat(result).containsEntry("functionName2", "x2");
        assertThat(result).containsEntry("number", 123);
    }
}
