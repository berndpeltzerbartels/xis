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

    @Test
    void simpleArrayTest() throws ScriptException {
        var result = evaluate("[1,2,3]", "{}");
        assertThat(result.getArraySize()).isEqualTo(3);
        assertThat(result.getArrayElement(0).asInt()).isEqualTo(1);
        assertThat(result.getArrayElement(1).asInt()).isEqualTo(2);
        assertThat(result.getArrayElement(2).asInt()).isEqualTo(3);
    }

    @Test
    void arrayInFunctionTest() throws ScriptException {
        var result = evaluate("sum([1,2,3])", "{}");
        assertThat(result.asInt()).isEqualTo(6);
    }


    @Test
    void simpleNumberTest() throws ScriptException {
        var result = evaluate("123", "{}");
        assertThat(result.asInt()).isEqualTo(123);
    }


    @Test
    void simpleStringTest() throws ScriptException {
        var result = evaluate("'123'", "{}");
        assertThat(result.asString()).isEqualTo("123");
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

    /**
     * Is testing the ternary operator in different scenarios.
     * Simple ternary operator, nested ternary operator, ternary operator with method calls etc.
     */
    @Nested
    class TernaryOperatorTest {

        @Test
        @DisplayName("Simple ternary operator")
        void testSimpleTernaryOperator() throws ScriptException {
            var result = evaluate("a > b ? a : b", "{a: 3, b: 2}");
            assertThat(result.asInt()).isEqualTo(3);
        }

        @Test
        @DisplayName("Simple ternary operator with false condition")
        void testSimpleTernaryOperatorWithFalseCondition() throws ScriptException {
            var result = evaluate("a > b ? a : b", "{a: 1, b: 2}");
            assertThat(result.asInt()).isEqualTo(2);
        }

        @Test
        @DisplayName("Nested ternary operator")
        void testNestedTernaryOperator() throws ScriptException {
            var result = evaluate("a > b ? (a > c ? a : c) : (b > c ? b : c)", "{a: 3, b: 2, c: 1}");
            assertThat(result.asInt()).isEqualTo(3);
        }

        @Test
        @DisplayName("Nested ternary operator with false condition")
        void testNestedTernaryOperatorWithFalseCondition() throws ScriptException {
            var result = evaluate("a > b ? (a > c ? a : c) : (b > c ? b : c)", "{a: 2, b: 3, c: 1}");
            assertThat(result.asInt()).isEqualTo(3);
        }

        @Test
        @DisplayName("Ternary operator as method parameter")
        void testTernaryOperatorAsMethodParameter() throws ScriptException {
            var result = evaluate("xyz(bool(a,b) ? a : b , b) : a", "{a: 3, b: 2}");
            assertThat(result.asInt()).isEqualTo(5);
        }


        @Test
        @DisplayName("Ternary operator with method calls")
        void testTernaryOperatorWithMethodCalls() throws ScriptException {
            var result = evaluate("bool(a, b) ? xyz(a, b) : a", "{a: 2, b: 3}");
            assertThat(result.asInt()).isEqualTo(2);
        }

        @Test
        @DisplayName("Ternary operator with method calls and nested ternary operator")
        void testTernaryOperatorWithMethodCallsAndNestedTernaryOperator() throws ScriptException {
            var result = evaluate("bool(a, b) ? (xyz(a, b) > 2 ? xyz(a, b) : 2) : a", "{a: 3, b: 2}");
            assertThat(result.asInt()).isEqualTo(5);
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

    @Nested
    class NegationTest {

        @Test
        void notTrue() throws ScriptException {
            var result = evaluate("!true", "{}");
            assertThat(result.asBoolean()).isFalse();
        }

        @Test
        void notFalse() throws ScriptException {
            var result = evaluate("!false", "{}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        void notExpression() throws ScriptException {
            var result = evaluate("!(a > b)", "{a: 2, b: 3}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        void notExpression2() throws ScriptException {
            var result = evaluate("!(a > b)", "{a: 3, b: 2}");
            assertThat(result.asBoolean()).isFalse();
        }

        @Test
        void notExpression3() throws ScriptException {
            var result = evaluate("!bool(a,b)||true", "{a: 2, b: 1}");
            assertThat(result.asBoolean()).isTrue();
        }
    }


    @Nested
    @DisplayName("Method Calls")
    class MethodCallTests {

        @Test
        @DisplayName("Simple method call on string - toUpperCase()")
        void testSimpleMethodCall() throws ScriptException {
            var result = evaluate("text.toUpperCase()", "{text: 'hello'}");
            assertThat(result.asString()).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("Method call with parameter - substring()")
        void testMethodCallWithParameter() throws ScriptException {
            var result = evaluate("text.substring(0, 3)", "{text: 'hello world'}");
            assertThat(result.asString()).isEqualTo("hel");
        }

        @Test
        @DisplayName("Chained method calls - trim().toUpperCase()")
        void testChainedMethodCalls() throws ScriptException {
            var result = evaluate("text.trim().toUpperCase()", "{text: '  hello  '}");
            assertThat(result.asString()).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("Method call on nested object - user.name.toUpperCase()")
        void testMethodCallOnNestedObject() throws ScriptException {
            var result = evaluate("user.name.toUpperCase()", "{user: {name: 'john'}}");
            assertThat(result.asString()).isEqualTo("JOHN");
        }

        @Test
        @DisplayName("Array length property access")
        void testArrayLength() throws ScriptException {
            var result = evaluate("items.length", "{items: [1, 2, 3]}");
            assertThat(result.asInt()).isEqualTo(3);
        }

        @Test
        @DisplayName("Method call with expression as parameter")
        void testMethodCallWithExpressionParameter() throws ScriptException {
            var result = evaluate("text.substring(start, start + 3)", "{text: 'hello world', start: 6}");
            assertThat(result.asString()).isEqualTo("wor");
        }

        @Test
        @DisplayName("String replace method")
        void testReplaceMethod() throws ScriptException {
            var result = evaluate("text.replace('world', 'there')", "{text: 'hello world'}");
            assertThat(result.asString()).isEqualTo("hello there");
        }

        @Test
        @DisplayName("Null safety - method call on null returns undefined")
        void testMethodCallOnNull() throws ScriptException {
            var result = evaluate("text.toUpperCase()", "{text: null}");
            assertThat(result.isNull()).isTrue();
        }

        @Test
        @DisplayName("Method call in arithmetic expression")
        void testMethodCallInArithmetic() throws ScriptException {
            var result = evaluate("items.length + 5", "{items: [1, 2, 3]}");
            assertThat(result.asInt()).isEqualTo(8);
        }

        @Test
        @DisplayName("Method call in conditional expression")
        void testMethodCallInConditional() throws ScriptException {
            var result = evaluate("text.length > 5", "{text: 'hello world'}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("Triple chained method calls")
        void testTripleChainedMethodCalls() throws ScriptException {
            var result = evaluate("text.trim().substring(0, 5).toUpperCase()", "{text: '  hello world  '}");
            assertThat(result.asString()).isEqualTo("HELLO");
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
                
                     function arraySum(arr){
                         var sum = 0;
                         for(var i=0; i<arr.length; i++){
                             sum += arr[i];
                         }
                         return sum;
                     }
                
                    var data = new Data(${data});
                    var expressionParser = new ExpressionParser({xyz: testFunction, bool: boolFunction, sum: arraySum});
                    var expression = expressionParser.parse("${expression}");
                    expression.evaluate(data);
                """).replace("${expression}", expression).replace("${data}", data);
        return JSUtil.execute(testScript);
    }
}
