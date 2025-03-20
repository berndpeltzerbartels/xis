package one.xis.js.parse;

import one.xis.js.Javascript;
import one.xis.test.js.JSUtil;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

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
    void methodWith2Parameters() throws ScriptException {
        var result = evaluate("xyz(a,b)", "{a: 1, b: 2}");

        assertThat(result.asInt()).isEqualTo(3);
    }

    @Test
    void methodAsMethodParameter() throws ScriptException {
        var result = evaluate("xyz(a,xyz(a,b))", "{a: 1, b: 2}");

        assertThat(result.asInt()).isEqualTo(4);
    }

    @Test
    void brackets() throws ScriptException {
        var result = evaluate("a*(b+c)", "{a: 2, b: 3, c: 4}");
        assertThat(result.asInt()).isEqualTo(14);
    }

    @Nested
    @DisplayName("a > 2 || b > 3")
    class GreaterAndOrTest {
        private static final String EXPRESSION = "a > 2 || b > 3";

        @Test
        @DisplayName("{a: 3, b: 2}")
        void test1() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: 3, b: 2}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("{a: 1, b: 4}")
        void test2() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: 1, b: 4}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("{a: 1, b: 2}")
        void test3() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: 1, b: 2}");
            assertThat(result.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("{a: 3, b: 4}")
        void test4() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: 3, b: 4}");
            assertThat(result.asBoolean()).isTrue();
        }
    }


    private Value evaluate(String expression, String data) throws ScriptException {
        var testScript = (javascript + """
                
                    function testFunction(p1, p2){
                          return p1 + p2;
                     }
                
                    var data = new Data(${data});
                    var expressionParser = new ExpressionParser({xyz: testFunction});
                    var expression = expressionParser.parse("${expression}");
                    expression.evaluate(data);
                """).replace("${expression}", expression).replace("${data}", data);
        return JSUtil.execute(testScript);
    }
}
