package one.xis.js;

import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.script.ScriptException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FunctionsTest {

    private String functions;

    @BeforeAll
    void loadFunctions() {
        functions = Javascript.getScript(JavascriptSource.FUNCTIONS);
    }

    @Test
    void trim() throws ScriptException {
        var strings = "['xyz',' x',' x',' x y ']";
        var script = functions + "var a = " + strings + "; a.map(v => trim(v));";

        var result = (List<String>) JSUtil.execute(script).as(List.class);

        assertThat(result).containsExactly("xyz", "x", "x", "x y");
    }

    @Test
    void cloneArr() throws ScriptException {
        var result = (List<Integer>) JSUtil.execute(functions + "cloneArr([1,2,3])").as(List.class);
        assertThat(result).containsExactly(1, 2, 3);
    }

    @Test
    void appendQueryParameters1() throws ScriptException {
        var result = JSUtil.execute(functions + "appendQueryParameters('http://example.com', {a: 'b', c: 'd'})").asString();
        assertThat(result).isEqualTo("http://example.com?a=b&c=d");
    }

    @Test
    void appendQueryParameters2() throws ScriptException {
        var result = JSUtil.execute(functions + "appendQueryParameters('http://example.com', {})").asString();
        assertThat(result).isEqualTo("http://example.com");
    }

    @Test
    void appendQueryParameters3() throws ScriptException {
        var result = JSUtil.execute(functions + "appendQueryParameters('http://example.com?a=b', {c: 'd'})").asString();
        assertThat(result).isEqualTo("http://example.com?a=b&c=d");
    }

    @Test
    void randomStringUsesFixedLengthAlphanumericCharacters() throws ScriptException {
        var result = JSUtil.execute(functions + "randomString()").asString();

        assertThat(result).hasSize(24);
        assertThat(result).matches("[A-Za-z0-9]+");
    }

    @Test
    void handleErrorDisplaysHttpObjectWithoutLosingOriginalError() throws ScriptException {
        var script = functions + """
                var shown = [];
                var app = {messageHandler: {addErrorMessage: function(message) { shown.push(message); }}};
                var console = {error: function() {}};
                var error = {
                    status: 0,
                    statusText: '',
                    responseText: '',
                    responseURL: '/xis/page/model',
                    toString: function() { return '[object XMLHttpRequest]'; }
                };
                var originalPreserved = false;
                try {
                    handleError(error);
                } catch (e) {
                    originalPreserved = e === error;
                }
                originalPreserved + ':' + shown[0];
                """;

        var result = JSUtil.execute(script).asString();

        assertThat(result).isEqualTo("true:Unhandled error: HTTP request failed (0): /xis/page/model");
    }

    @Test
    void handleErrorReadsHttpMessageFromEventTarget() throws ScriptException {
        var script = functions + """
                var shown = [];
                var app = {messageHandler: {addErrorMessage: function(message) { shown.push(message); }}};
                var console = {error: function() {}};
                var event = {
                    type: 'error',
                    target: {
                        status: 500,
                        statusText: 'Server Error',
                        responseText: '{"message":"Backend exploded"}',
                        responseURL: '/xis/frontlet/action'
                    },
                    toString: function() { return '[object Event]'; }
                };
                var originalPreserved = false;
                try {
                    handleError(event);
                } catch (e) {
                    originalPreserved = e === event;
                }
                originalPreserved + ':' + shown[0];
                """;

        var result = JSUtil.execute(script).asString();

        assertThat(result).isEqualTo("true:Unhandled error: Backend exploded");
    }

}
