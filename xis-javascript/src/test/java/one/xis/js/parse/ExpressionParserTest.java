package one.xis.js.parse;

import one.xis.js.Javascript;
import one.xis.test.js.JSUtil;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.*;

import javax.script.ScriptException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static one.xis.js.JavascriptSource.CLASSES;
import static one.xis.js.JavascriptSource.FUNCTIONS;
import static org.assertj.core.api.Assertions.assertThat;

class ExpressionParserTest {
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
        @DisplayName("{a: false, b: true, c: true}")
        void test3() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: false, b: true, c: true}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("{a: false, b: false, c: true}")
        void test4() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: false, b: false, c: true}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("{a: true, b: true, c: false}")
        void test5() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: true, b: true, c: false}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("{a: true, b: false, c: false}")
        void test6() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: true, b: false, c: false}");
            assertThat(result.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("{a: false, b: true, c: false}")
        void test7() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: false, b: true, c: false}");
            assertThat(result.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("{a: false, b: false, c: false}")
        void test8() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: false, b: false, c: false}");
            assertThat(result.asBoolean()).isFalse();
        }

    }

    @Test
    @DisplayName("{a: false, b: false, c: false}")
    void plusAndMultiplication() throws ScriptException {
        var result = evaluate("a+b*c", "{a: 2, b: 3, c: 4}");
        assertThat(result.asInt()).isEqualTo(14);
    }


    @Test
    @Disabled
    void methodWithVarParameter() throws ScriptException {
        var testScript = javascript + "expressionParser.parse('xyz(a.b)');";

        var result = JSUtil.execute(testScript);

        assertThat(result.getMember("type").asString()).isEqualTo("FUNCTION");
        assertThat(result.getMember("next").asString()).isNull();
        assertThat(result.getMember("name").asString()).isEqualTo("xyz");

        var parameters = (List<Object>) result.getMember("parameters").as(List.class);
        assertThat(parameters).hasSize(1);

        var parameter = (Map<String, Object>) parameters.get(0);
        assertThat(parameter.get("type")).isEqualTo("VAR");
        assertThat((Collection<String>) parameter.get("path")).containsExactly("a", "b");
        assertThat(parameter.get("next")).isNull(); // TODO check with function in b
    }


    private Value evaluate(String expression, String data) throws ScriptException {
        var testScript = (javascript + """
                var data = new Data(${data});
                var expressionParser = new ExpressionParser();
                var expression = expressionParser.parse("${expression}");
                expression.evaluate(data);
                """).replace("${expression}", expression).replace("${data}", data);
        return JSUtil.execute(testScript);
    }
}
