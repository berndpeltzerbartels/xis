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

    @Test
    void arithmetic() throws ScriptException {
        var result = evaluate("a + b * c", "{a: 2, b: 3, c: 4}");
        assertThat(result.asInt()).isEqualTo(14);
    }

    @Test
    void arithmeticWithBrackets() throws ScriptException {
        var result = evaluate("d * (a + b * c)", "{a: 2, b: 3, c: 4, d: 4}");
        assertThat(result.asInt()).isEqualTo(56);
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

    @Nested
    @DisplayName("Comparison Test")
    class ComparisonTest {

        @Test
        @DisplayName("Greater than comparison with addition")
        void testGreaterThanWithAddition() throws ScriptException {
            var result = evaluate("a + b > c", "{a: 2, b: 3, c: 4}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("Less than comparison with subtraction")
        void testLessThanWithSubtraction() throws ScriptException {
            var result = evaluate("a - b < c", "{a: 5, b: 2, c: 4}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("Equal comparison with multiplication")
        void testEqualWithMultiplication() throws ScriptException {
            var result = evaluate("a * b == c", "{a: 2, b: 3, c: 6}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("Not equal comparison with division")
        void testNotEqualWithDivision() throws ScriptException {
            var result = evaluate("a / b != c", "{a: 6, b: 2, c: 4}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("Greater or equal comparison with constant")
        void testGreaterOrEqualWithConstant() throws ScriptException {
            var result = evaluate("a >= 5", "{a: 5}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("Less or equal comparison with constant")
        void testLessOrEqualWithConstant() throws ScriptException {
            var result = evaluate("a <= 3", "{a: 2}");
            assertThat(result.asBoolean()).isTrue();
        }
    }

    @Nested
    @DisplayName("Brackets Test")
    class BracketsTest {

        @Test
        @DisplayName("Simple nested brackets")
        void testSimpleNestedBrackets() throws ScriptException {
            var result = evaluate("(a + b) * (c - d)", "{a: 2, b: 3, c: 5, d: 1}");
            assertThat(result.asInt()).isEqualTo(20);
        }

        @Test
        @DisplayName("Multiple nested brackets")
        void testMultipleNestedBrackets() throws ScriptException {
            var result = evaluate("((a + b) * (c - d)) + ((e + f) * (g - h))", "{a: 2, b: 3, c: 5, d: 1, e: 1, f: 2, g: 4, h: 2}");
            assertThat(result.asInt()).isEqualTo(26);
        }

        @Test
        @DisplayName("Nested brackets with functions")
        void testNestedBracketsWithFunctions() throws ScriptException {
            var result = evaluate("xyz((a + b), (c - d))", "{a: 2, b: 3, c: 5, d: 1}");
            assertThat(result.asInt()).isEqualTo(9);
        }

        @Test
        @DisplayName("Complex nested brackets")
        void testComplexNestedBrackets() throws ScriptException {
            var result = evaluate("((a + b) * (c - d)) + xyz((e + f), (g - h))", "{a: 2, b: 3, c: 5, d: 1, e: 1, f: 2, g: 4, h: 2}");
            assertThat(result.asInt()).isEqualTo(25);
        }
    }

    @Nested
    class ObjectPropertyTest {

        @Test
        @DisplayName("Object property access")
        void testSimpleObjectPropertyAccess() throws ScriptException {
            var result = evaluate("a.b", "{a: {b: 8}}");
            assertThat(result.asInt()).isEqualTo(8);
        }

        @Test
        @DisplayName("Object property access by key")
        void testSimpleObjectPropertyAccessByKey() throws ScriptException {
            var result = evaluate("a['b']", "{a: {b: 8}}");
            assertThat(result.asInt()).isEqualTo(8);
        }

        @Test
        @DisplayName("Object property access by key and addition")
        void testObjectPropertyAccessByKeyWithAddition() throws ScriptException {
            var result = evaluate("2 + a['b']", "{a: {b: 8}}");
            assertThat(result.asInt()).isEqualTo(10);
        }

        @Test
        @DisplayName("Object property access by variable string key")
        void testObjectPropertyAccessByVariableStringKey() throws ScriptException {
            var result = evaluate("a[c]", "{a: {b: 8}, c: 'b'}");
            assertThat(result.asInt()).isEqualTo(8);
        }

        @Test
        void arrayElementAccess() throws ScriptException {
            var result = evaluate("a[1]", "{a: [1, 2, 3]}");
            assertThat(result.asInt()).isEqualTo(2);
        }

        @Test
        void arrayElementAccessWithAddition() throws ScriptException {
            var result = evaluate("a[1] + a[2]", "{a: [1, 2, 3]}");
            assertThat(result.asInt()).isEqualTo(5);
        }

        @Test
        void arrayElementAccessWithVariableIndex() throws ScriptException {
            var result = evaluate("a[b]", "{a: [1, 2, 3], b: 1}");
            assertThat(result.asInt()).isEqualTo(2);
        }

        @Test
        void arrayElementAccessWithVariableIndexFromFunction() throws ScriptException {
            var result = evaluate("a[xyz(1, 2) - 1]", "{a: [1, 2, 3]}");
            assertThat(result.asInt()).isEqualTo(3);
        }

        @Test
        void arrayElementAccessWithVariableIndexFromFunction2() throws ScriptException {
            var result = evaluate("a[-2 + xyz(1, 2)]", "{a: [10, 20, 30]}");
            assertThat(result.asInt()).isEqualTo(20);
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
        @DisplayName("1. {a: 3, b: 2}")
        void test1() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: 3, b: 2}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("2. {a: 1, b: 4}")
        void test2() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: 1, b: 4}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("3. {a: 1, b: 2}")
        void test3() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: 1, b: 2}");
            assertThat(result.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("4. {a: 3, b: 4}")
        void test4() throws ScriptException {
            var result = evaluate(EXPRESSION, "{a: 3, b: 4}");
            assertThat(result.asBoolean()).isTrue();
        }
    }

    @Nested
    @DisplayName("Complex Expression Test")
    class ComplexExpressionTest {
        private static final String EXPRESSION = "((v1 + v2) > v3 && xyz(v1, v2) < v3) || bool(v1, v2)";

        @Test
        @DisplayName("1. {v1: 1, v2: 2, v3: 4}")
        void test1() throws ScriptException {
            var result = evaluate(EXPRESSION, "{v1: 1, v2: 2, v3: 4}");
            assertThat(result.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("2. {v1: 3, v2: 2, v3: 4}")
        void test2() throws ScriptException {
            var result = evaluate(EXPRESSION, "{v1: 3, v2: 2, v3: 4}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("3. {v1: 5, v2: 3, v3: 7}")
        void test3() throws ScriptException {
            var result = evaluate(EXPRESSION, "{v1: 5, v2: 3, v3: 7}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("4. {v1: 2, v2: 2, v3: 5}")
        void test4() throws ScriptException {
            var result = evaluate(EXPRESSION, "{v1: 2, v2: 2, v3: 5}");
            assertThat(result.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("5. {v1: 4, v2: 4, v3: 8}")
        void test5() throws ScriptException {
            var result = evaluate(EXPRESSION, "{v1: 4, v2: 4, v3: 8}");
            assertThat(result.asBoolean()).isFalse();
        }
    }


    private Value evaluate(String expression, String data) throws ScriptException {
        var testScript = (javascript + """
                
                    function testFunction(p1, p2){
                          return p1 + p2;
                     }
                
                     function boolFunction(p1, p2){
                          return p1 > p2;
                     }
                
                    var data = new Data(${data});
                    var expressionParser = new ExpressionParser({xyz: testFunction, bool: boolFunction});
                    var expression = expressionParser.parse("${expression}");
                    expression.evaluate(data);
                """).replace("${expression}", expression).replace("${data}", data);
        return JSUtil.execute(testScript);
    }
}
