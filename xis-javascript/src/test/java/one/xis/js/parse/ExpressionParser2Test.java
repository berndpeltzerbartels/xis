package one.xis.js.parse;

import one.xis.js.Javascript;
import one.xis.test.js.JSUtil;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.*;

import javax.script.ScriptException;

import static one.xis.js.JavascriptSource.CLASSES;
import static one.xis.js.JavascriptSource.FUNCTIONS;
import static org.assertj.core.api.Assertions.assertThat;

class ExpressionParser2Test {
    private String javascript;

    @BeforeEach
    void init() {
        javascript = Javascript.getScript(CLASSES, FUNCTIONS);
    }

    @Test
    void aAndB() throws ScriptException {
        var result = evaluate("a && b", "{a: true, b: true}");
        assertThat(result.asBoolean()).isTrue();
    }

    @Test
    void aOrB() throws ScriptException {
        var result = evaluate("a || b", "{a: false, b: true}");
        assertThat(result.asBoolean()).isTrue();
    }

    @Nested
    @DisplayName("a && b || c")
    class AAndBOrCTest {
        private static final String EXPRESSION = "a && b || c";

        @Test
        @DisplayName("{a: true, b: true, c: true}")
        void test1() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: true, b: true, c: true}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("{a: true, b: false, c: true}")
        void test2() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: true, b: false, c: true}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @Disabled
        @DisplayName("{a: false, b: true, c: true}")
        void test3() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: false, b: true, c: true}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @Disabled
        @DisplayName("{a: false, b: false, c: true}")
        void test4() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: false, b: false, c: true}");
            assertThat(result.asBoolean()).isTrue();
        }

    }

    @Test
    void aAndBOrC1() throws ScriptException {
        var result = evaluate("a && b || c", "{a: true, b: false, c: true}");
        assertThat(result.asBoolean()).isTrue();
    }


    private Value evaluate(String expression, String data) throws ScriptException {
        var testScript = (javascript + """
                var data = new Data(${data});
                var expressionParser = new ExpressionParser2();
                var expression = expressionParser.parse("${expression}");
                expression.evaluate(data);
                """).replace("${expression}", expression).replace("${data}", data);
        return JSUtil.execute(testScript);
    }
}
