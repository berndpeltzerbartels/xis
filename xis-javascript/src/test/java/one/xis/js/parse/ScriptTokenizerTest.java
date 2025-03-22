package one.xis.js.parse;

import one.xis.js.Javascript;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static one.xis.js.JavascriptSource.CLASSES;
import static org.assertj.core.api.Assertions.assertThat;

class ScriptTokenizerTest {

    private static final int STRING = 1;
    private static final int INTEGER = 2;
    private static final int FLOAT = 3;
    private static final int BOOL = 4;
    private static final int NULL_OR_UNDEFINED = 5;
    private static final int AND = 9;
    private static final int OR = 10;
    private static final int NOT = 11;
    private static final int EQUAL = 12;
    private static final int NOT_EQUAL = 13;
    private static final int GREATER = 14;
    private static final int GREATER_EQUAL = 15;
    private static final int LESS = 16;
    private static final int LESS_EQUAL = 17;
    private static final int ADD = 18;
    private static final int SUB = 19;
    private static final int MUL = 20;
    private static final int DIV = 21;
    private static final int MOD = 22;
    private static final int ASSIGN = 23;
    private static final int ADD_ASSIGN = 24;
    private static final int SUB_ASSIGN = 25;
    private static final int MUL_ASSIGN = 26;
    private static final int DIV_ASSIGN = 27;
    private static final int MOD_ASSIGN = 28;
    private static final int INCREMENT = 29;
    private static final int DECREMENT = 30;
    private static final int OPEN_BRACKET = 31;
    private static final int CLOSE_BRACKET = 32;
    private static final int IDENTIFIER = 34;
    private static final int OPENING_SQUARE_BRACKET = 37;
    private static final int CLOSING_SQUARE_BRACKET = 38;
    private static final int QUESTION_MARK = 39;
    private static final int COLON = 40;

    private String javascript;

    @BeforeEach
    void init() {
        javascript = Javascript.getScript(CLASSES);
    }

    @Nested
    class SimpleTests {

        @Test
        void testString() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('\"   \"').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(STRING);
            assertThat(value.getMember("value").asString()).isEqualTo("   ");
        }

        @Test
        void testInteger() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer(123).tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(INTEGER);
            assertThat(value.getMember("value").asInt()).isEqualTo(123);
        }

        @Test
        void testFloat() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer(123.45).tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(FLOAT);
            assertThat(value.getMember("value").asDouble()).isEqualTo(123.45);
        }

        @Test
        void testBooleanTrue() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('true').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(BOOL);
            assertThat(value.getMember("value").asBoolean()).isTrue();
        }

        @Test
        void testBooleanFalse() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('false').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(BOOL);
            assertThat(value.getMember("value").asBoolean()).isFalse();
        }

        @Test
        void testNull() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('null').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(NULL_OR_UNDEFINED);
            assertThat(value.getMember("value").isNull()).isTrue();
        }

        @Test
        void testArray() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('[]').tokenize();";
            var result = JSUtil.execute(testScript);
            var open = result.getArrayElement(0);
            var close = result.getArrayElement(1);
            assertThat(open.getMember("type").asInt()).isEqualTo(OPENING_SQUARE_BRACKET);
            assertThat(close.getMember("type").asInt()).isEqualTo(CLOSING_SQUARE_BRACKET);
        }

        @Test
        void testFunction() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('abc()').tokenize();";
            var result = JSUtil.execute(testScript);

            // we expect IDENTIFIED with name "abc", OPEN_BRACKET, CLOSE_BRACKET
            var identifier = result.getArrayElement(0);
            var openBracket = result.getArrayElement(1);
            var closeBracket = result.getArrayElement(2);

            assertThat(identifier.getMember("type").asInt()).isEqualTo(IDENTIFIER);
            assertThat(identifier.getMember("name").asString()).isEqualTo("abc");
            assertThat(openBracket.getMember("type").asInt()).isEqualTo(OPEN_BRACKET);
            assertThat(closeBracket.getMember("type").asInt()).isEqualTo(CLOSE_BRACKET);

        }

        @Test
        void testAnd() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('&&').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(AND);
        }

        @Test
        void testOr() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('||').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(OR);
        }

        @Test
        void testNot() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('!').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(NOT);
        }

        @Test
        void testEqual() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('==').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(EQUAL);
        }

        @Test
        void testNotEqual() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('!=').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(NOT_EQUAL);
        }

        @Test
        void testGreater() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('>').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(GREATER);
        }

        @Test
        void testGreaterEqual() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('>=').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(GREATER_EQUAL);
        }

        @Test
        void testLess() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('<').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(LESS);
        }

        @Test
        void testLessEqual() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('<=').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(LESS_EQUAL);
        }

        @Test
        void testAdd() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('+').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(ADD);
        }

        @Test
        void testSub() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('-').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(SUB);
        }

        @Test
        void testMul() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('*').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(MUL);
        }

        @Test
        void testDiv() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('/').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(DIV);
        }

        @Test
        void testMod() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('%').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(MOD);
        }

        @Test
        void testAssign() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('=').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(ASSIGN);
        }

        @Test
        void testAddAssign() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('+=').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(ADD_ASSIGN);
        }

        @Test
        void testSubAssign() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('-=').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(SUB_ASSIGN);
        }

        @Test
        void testMulAssign() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('*=').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(MUL_ASSIGN);
        }

        @Test
        void testDivAssign() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('/=').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(DIV_ASSIGN);
        }

        @Test
        void testModAssign() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('%=').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(MOD_ASSIGN);
        }

        @Test
        void testIncrement() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('++').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(INCREMENT);
        }

        @Test
        void testDecrement() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('--').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(DECREMENT);
        }

        @Test
        void testOpenBracket() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('(').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(OPEN_BRACKET);
        }

        @Test
        void testCloseBracket() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer(')').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(CLOSE_BRACKET);
        }

        @Test
        void testQuestionMark() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer('?').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(QUESTION_MARK);
        }

        @Test
        void testColon() throws ScriptException {
            var testScript = javascript + "new ScriptTokenizer(':').tokenize();";
            var result = JSUtil.execute(testScript);
            var value = result.getArrayElement(0);
            assertThat(value.getMember("type").asInt()).isEqualTo(COLON);
        }
    }

    @Test
    void simpleVar() throws ScriptException {
        var testScript = javascript + "new ScriptTokenizer('a').tokenize();";
        var result = JSUtil.execute(testScript);
        var value = result.getArrayElement(0);
        assertThat(value.getMember("type").asInt()).isEqualTo(IDENTIFIER);
        assertThat(value.getMember("name").asString()).isEqualTo("a");
    }

    @Test
    void objectVarWithField() throws ScriptException {
        var testScript = javascript + "new ScriptTokenizer('a.b').tokenize();";
        var result = JSUtil.execute(testScript);
        var value = result.getArrayElement(0);
        assertThat(value.getMember("type").asInt()).isEqualTo(IDENTIFIER);
        assertThat(value.getMember("name").asString()).isEqualTo("a.b");
    }


    @Test
    void twoVarsWithAND() throws ScriptException {
        var testScript = javascript + "new ScriptTokenizer('a && b').tokenize();";
        var result = JSUtil.execute(testScript);

        var a = result.getArrayElement(0);
        var and = result.getArrayElement(1);
        var b = result.getArrayElement(2);

        assertThat(a.getMember("type").asInt()).isEqualTo(IDENTIFIER);
        assertThat(a.getMember("name").asString()).isEqualTo("a");

        assertThat(and.getMember("type").asInt()).isEqualTo(AND);

        assertThat(b.getMember("type").asInt()).isEqualTo(IDENTIFIER);
        assertThat(b.getMember("name").asString()).isEqualTo("b");
    }

    @Test
    void checkStringEqual() throws ScriptException {
        var testScript = javascript + "new ScriptTokenizer('a == \"b\"').tokenize();";
        var result = JSUtil.execute(testScript);

        var a = result.getArrayElement(0);
        var equal = result.getArrayElement(1);
        var b = result.getArrayElement(2);

        assertThat(a.getMember("type").asInt()).isEqualTo(IDENTIFIER);
        assertThat(a.getMember("name").asString()).isEqualTo("a");

        assertThat(equal.getMember("type").asInt()).isEqualTo(EQUAL);

        assertThat(b.getMember("type").asInt()).isEqualTo(STRING);
        assertThat(b.getMember("value").asString()).isEqualTo("b");
    }


    @Test
    void complexExample() throws ScriptException {
        var testScript = javascript + "new ScriptTokenizer('fun1(a && b == \"c\" && fun2(a) == true)').tokenize();";
        var result = JSUtil.execute(testScript);

        var fun1 = result.getArrayElement(0);
        var openBracket1 = result.getArrayElement(1);
        var a = result.getArrayElement(2);
        var and1 = result.getArrayElement(3);
        var b = result.getArrayElement(4);
        var equal = result.getArrayElement(5);
        var c = result.getArrayElement(6);
        var and2 = result.getArrayElement(7);
        var fun2 = result.getArrayElement(8);
        var openBracket2 = result.getArrayElement(9);
        var a2 = result.getArrayElement(10);
        var closeBracket2 = result.getArrayElement(11);
        var equal2 = result.getArrayElement(12);
        var trueValue = result.getArrayElement(13);
        var closeBracket1 = result.getArrayElement(14);

        assertThat(fun1.getMember("type").asInt()).isEqualTo(IDENTIFIER);
        assertThat(fun1.getMember("name").asString()).isEqualTo("fun1");

        assertThat(openBracket1.getMember("type").asInt()).isEqualTo(OPEN_BRACKET);

        assertThat(a.getMember("type").asInt()).isEqualTo(IDENTIFIER);
        assertThat(a.getMember("name").asString()).isEqualTo("a");

        assertThat(and1.getMember("type").asInt()).isEqualTo(AND);

        assertThat(b.getMember("type").asInt()).isEqualTo(IDENTIFIER);
        assertThat(b.getMember("name").asString()).isEqualTo("b");

        assertThat(equal.getMember("type").asInt()).isEqualTo(EQUAL);

        assertThat(c.getMember("type").asInt()).isEqualTo(STRING);
        assertThat(c.getMember("value").asString()).isEqualTo("c");

        assertThat(and2.getMember("type").asInt()).isEqualTo(AND);

        assertThat(fun2.getMember("type").asInt()).isEqualTo(IDENTIFIER);

        assertThat(openBracket2.getMember("type").asInt()).isEqualTo(OPEN_BRACKET);

        assertThat(a2.getMember("type").asInt()).isEqualTo(IDENTIFIER);
        assertThat(a2.getMember("name").asString()).isEqualTo("a");

        assertThat(closeBracket2.getMember("type").asInt()).isEqualTo(CLOSE_BRACKET);

        assertThat(equal2.getMember("type").asInt()).isEqualTo(EQUAL);

        assertThat(trueValue.getMember("type").asInt()).isEqualTo(BOOL);
        assertThat(trueValue.getMember("value").asBoolean()).isTrue();

        assertThat(closeBracket1.getMember("type").asInt()).isEqualTo(CLOSE_BRACKET);
    }
}
