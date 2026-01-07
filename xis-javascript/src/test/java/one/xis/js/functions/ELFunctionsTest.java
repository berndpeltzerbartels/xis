package one.xis.js.functions;

import one.xis.context.BrowserFunctions;
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

class ELFunctionsTest {
    private String javascript;

    @BeforeEach
    void init() {
        javascript = Javascript.getScript(CLASSES, FUNCTIONS);
    }

    @Nested
    @DisplayName("length()")
    class LengthTest {

        @Test
        @DisplayName("length of array")
        void arrayLength() throws ScriptException {
            var result = evaluate("length(items)", "{items: [1, 2, 3, 4, 5]}");
            assertThat(result.asInt()).isEqualTo(5);
        }

        @Test
        @DisplayName("length of empty array")
        void emptyArrayLength() throws ScriptException {
            var result = evaluate("length(items)", "{items: []}");
            assertThat(result.asInt()).isEqualTo(0);
        }

        @Test
        @DisplayName("length of string")
        void stringLength() throws ScriptException {
            var result = evaluate("length(text)", "{text: 'hello'}");
            assertThat(result.asInt()).isEqualTo(5);
        }

        @Test
        @DisplayName("length of empty string")
        void emptyStringLength() throws ScriptException {
            var result = evaluate("length(text)", "{text: ''}");
            assertThat(result.asInt()).isEqualTo(0);
        }

        @Test
        @DisplayName("length of null")
        void nullLength() throws ScriptException {
            var result = evaluate("length(value)", "{value: null}");
            assertThat(result.asInt()).isEqualTo(0);
        }

        @Test
        @DisplayName("length of undefined")
        void undefinedLength() throws ScriptException {
            var result = evaluate("length(value)", "{}");
            assertThat(result.asInt()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("sum()")
    class SumTest {

        @Test
        @DisplayName("sum of numbers")
        void sumNumbers() throws ScriptException {
            var result = evaluate("sum(items)", "{items: [1, 2, 3, 4, 5]}");
            assertThat(result.asInt()).isEqualTo(15);
        }

        @Test
        @DisplayName("sum of floats")
        void sumFloats() throws ScriptException {
            var result = evaluate("sum(prices)", "{prices: [10.99, 25.50, 15.75]}");
            assertThat(result.asDouble()).isEqualTo(52.24);
        }

        @Test
        @DisplayName("sum of empty array is 0")
        void sumEmpty() throws ScriptException {
            var result = evaluate("sum(items)", "{items: []}");
            assertThat(result.asDouble()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("sum of undefined is 0")
        void sumObjectProperty() throws ScriptException {
            var result = evaluate("sum()", "undefined");
            assertThat(result.asDouble()).isEqualTo(0.0);
        }


        @Test
        @DisplayName("sum with null values")
        void sumWithNull() throws ScriptException {
            var result = evaluate("sum(items)", "{items: [1, null, 3, null, 5]}");
            assertThat(result.asDouble()).isEqualTo(9.0);
        }


    }

    @Nested
    @DisplayName("map()")
    class MapTest {

        @Test
        @DisplayName("flat map complex property")
        void mapProperty() throws ScriptException {
            var result = evaluate("flatMap(company, path)", """
                    {
                    company: {
                        departments: [
                            {
                                name: 'HR',
                                employees: [
                                    {name: 'John', wage: 50000},                     
                                    {name: 'Jane', wage: 60000},
                                    {name: 'Bob', wage: 55000}
                                ]
                            },
                            {  
                                name: 'IT',
                                employees: [
                                    {name: 'Alice', wage: 70000},                     
                                    {name: 'Tom', wage: 65000}
                                ]
                            }
                        ]
                    },
                    path: 'departments.employees.wage'
                    }
                    """);
            assertThat(result.hasArrayElements()).isTrue();
            assertThat(result.getArraySize()).isEqualTo(5);
            assertThat(result.getArrayElement(0).asInt()).isEqualTo(50000);
            assertThat(result.getArrayElement(1).asInt()).isEqualTo(60000);
            assertThat(result.getArrayElement(2).asInt()).isEqualTo(55000);
            assertThat(result.getArrayElement(3).asInt()).isEqualTo(70000);
            assertThat(result.getArrayElement(4).asInt()).isEqualTo(65000);
        }

    }

    @Nested
    @DisplayName("contains()")
    class ContainsTest {

        @Test
        @DisplayName("contains existing element")
        void containsExists() throws ScriptException {
            var result = evaluate("contains(items, value)", "{items: [1, 2, 3, 4, 5], value: 3}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("contains non-existing element")
        void containsNotExists() throws ScriptException {
            var result = evaluate("contains(items, value)", "{items: [1, 2, 3, 4, 5], value: 6}");
            assertThat(result.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("contains in empty array")
        void containsEmpty() throws ScriptException {
            var result = evaluate("contains(items, value)", "{items: [], value: 1}");
            assertThat(result.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("contains string")
        void containsString() throws ScriptException {
            var result = evaluate("contains(fruits, 'banana')", "{fruits: ['apple', 'banana', 'cherry']}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("contains in null array")
        void containsNull() throws ScriptException {
            var result = evaluate("contains(items, value)", "{items: null, value: 1}");
            assertThat(result.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("contains with literal value")
        void containsWithLiteral() throws ScriptException {
            var result = evaluate("contains(items, 3)", "{items: [1, 2, 3, 4, 5]}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("contains with literal string")
        void containsWithLiteralString() throws ScriptException {
            var result = evaluate("contains(fruits, 'banana')", "{fruits: ['apple', 'banana', 'cherry']}");
            assertThat(result.asBoolean()).isTrue();
        }
    }

    @Nested
    @DisplayName("toUpperCase()")
    class ToUpperCaseTest {

        @Test
        @DisplayName("uppercase lowercase string")
        void uppercaseLowercase() throws ScriptException {
            var result = evaluate("toUpperCase(text)", "{text: 'hello world'}");
            assertThat(result.asString()).isEqualTo("HELLO WORLD");
        }

        @Test
        @DisplayName("uppercase mixed case string")
        void uppercaseMixed() throws ScriptException {
            var result = evaluate("toUpperCase(text)", "{text: 'HeLLo WoRLd'}");
            assertThat(result.asString()).isEqualTo("HELLO WORLD");
        }

        @Test
        @DisplayName("uppercase empty string")
        void uppercaseEmpty() throws ScriptException {
            var result = evaluate("toUpperCase(text)", "{text: ''}");
            assertThat(result.asString()).isEqualTo("");
        }

        @Test
        @DisplayName("uppercase null")
        void uppercaseNull() throws ScriptException {
            var result = evaluate("toUpperCase(text)", "{text: null}");
            assertThat(result.asString()).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("toLowerCase()")
    class ToLowerCaseTest {

        @Test
        @DisplayName("lowercase uppercase string")
        void lowercaseUppercase() throws ScriptException {
            var result = evaluate("toLowerCase(text)", "{text: 'HELLO WORLD'}");
            assertThat(result.asString()).isEqualTo("hello world");
        }

        @Test
        @DisplayName("lowercase mixed case string")
        void lowercaseMixed() throws ScriptException {
            var result = evaluate("toLowerCase(text)", "{text: 'HeLLo WoRLd'}");
            assertThat(result.asString()).isEqualTo("hello world");
        }

        @Test
        @DisplayName("lowercase empty string")
        void lowercaseEmpty() throws ScriptException {
            var result = evaluate("toLowerCase(text)", "{text: ''}");
            assertThat(result.asString()).isEqualTo("");
        }

        @Test
        @DisplayName("lowercase null")
        void lowercaseNull() throws ScriptException {
            var result = evaluate("toLowerCase(text)", "{text: null}");
            assertThat(result.asString()).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("formatDate()")
    class FormatDateTest {

        @Test
        @DisplayName("format date with default format")
        void formatDateDefault() throws ScriptException {
            var result = evaluate("formatDate(date)", "{date: new Date('2026-01-07T12:30:00')}");
            assertThat(result.asString()).matches("\\d{2}\\.\\d{2}\\.\\d{4}");
        }

        @Test
        @DisplayName("format date with custom format")
        void formatDateCustom() throws ScriptException {
            var result = evaluate("formatDate(date, locale)", "{date: new Date('2026-01-07T12:30:00'), locale: 'de-DE'}");
            assertThat(result.asString()).isEqualTo("07.01.2026");
        }

        @Test
        @DisplayName("format date with time")
        void formatDateTime() throws ScriptException {
            var result = evaluate("formatDateTime(date, locale)", "{date: new Date('2026-01-07T12:30:00'), locale: 'de-DE'}");
            assertThat(result.asString()).isEqualTo("07.01.2026, 12:30");
        }

        @Test
        @DisplayName("format null date")
        void formatNullDate() throws ScriptException {
            var result = evaluate("formatDate(date)", "{date: null}");
            assertThat(result.asString()).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("join()")
    class JoinTest {

        @Test
        @DisplayName("join with comma")
        void joinComma() throws ScriptException {
            var result = evaluate("join(fruits, ', ')", "{fruits: ['apple', 'banana', 'cherry']}");
            assertThat(result.asString()).isEqualTo("apple, banana, cherry");
        }

        @Test
        @DisplayName("join with space")
        void joinSpace() throws ScriptException {
            var result = evaluate("join(words, ' ')", "{words: ['Hello', 'World']}");
            assertThat(result.asString()).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("join empty array")
        void joinEmpty() throws ScriptException {
            var result = evaluate("join(items, ', ')", "{items: []}");
            assertThat(result.asString()).isEqualTo("");
        }

        @Test
        @DisplayName("join null array")
        void joinNull() throws ScriptException {
            var result = evaluate("join(items, ', ')", "{items: null}");
            assertThat(result.asString()).isEqualTo("");
        }

        @Test
        @DisplayName("join numbers")
        void joinNumbers() throws ScriptException {
            var result = evaluate("join(numbers, '-')", "{numbers: [1, 2, 3, 4, 5]}");
            assertThat(result.asString()).isEqualTo("1-2-3-4-5");
        }
    }

    @Nested
    @DisplayName("filter()")
    class FilterTest {

        @Test
        @DisplayName("filter by property value")
        void filterByProperty() throws ScriptException {
            var result = evaluate("filter(items, 'active', true)", "{items: [{active: true, name: 'A'}, {active: false, name: 'B'}, {active: true, name: 'C'}]}");
            assertThat(result.hasArrayElements()).isTrue();
            assertThat(result.getArraySize()).isEqualTo(2);
            assertThat(result.getArrayElement(0).getMember("name").asString()).isEqualTo("A");
            assertThat(result.getArrayElement(1).getMember("name").asString()).isEqualTo("C");
        }

        @Test
        @DisplayName("filter by nested property")
        void filterByNestedProperty() throws ScriptException {
            var result = evaluate("filter(items, 'user.role', 'admin')", "{items: [{user: {role: 'admin'}}, {user: {role: 'user'}}, {user: {role: 'admin'}}]}");
            assertThat(result.hasArrayElements()).isTrue();
            assertThat(result.getArraySize()).isEqualTo(2);
        }

        @Test
        @DisplayName("filter empty array")
        void filterEmpty() throws ScriptException {
            var result = evaluate("filter(items, 'active', true)", "{items: []}");
            assertThat(result.hasArrayElements()).isTrue();
            assertThat(result.getArraySize()).isEqualTo(0);
        }

        @Test
        @DisplayName("filter null array")
        void filterNull() throws ScriptException {
            var result = evaluate("filter(items, 'active', true)", "{items: null}");
            assertThat(result.hasArrayElements()).isTrue();
            assertThat(result.getArraySize()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("isEmpty()")
    class IsEmptyTest {

        @Test
        @DisplayName("isEmpty on empty array")
        void isEmptyArray() throws ScriptException {
            var result = evaluate("isEmpty(items)", "{items: []}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("isEmpty on non-empty array")
        void isNotEmptyArray() throws ScriptException {
            var result = evaluate("isEmpty(items)", "{items: [1, 2, 3]}");
            assertThat(result.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("isEmpty on empty string")
        void isEmptyString() throws ScriptException {
            var result = evaluate("isEmpty(text)", "{text: ''}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("isEmpty on non-empty string")
        void isNotEmptyString() throws ScriptException {
            var result = evaluate("isEmpty(text)", "{text: 'hello'}");
            assertThat(result.asBoolean()).isFalse();
        }

        @Test
        @DisplayName("isEmpty on null")
        void isEmptyNull() throws ScriptException {
            var result = evaluate("isEmpty(value)", "{value: null}");
            assertThat(result.asBoolean()).isTrue();
        }

        @Test
        @DisplayName("isEmpty on undefined")
        void isEmptyUndefined() throws ScriptException {
            var result = evaluate("isEmpty(value)", "{}");
            assertThat(result.asBoolean()).isTrue();
        }
    }

    @Nested
    @DisplayName("defaultValue()")
    class DefaultValueTest {

        @Test
        @DisplayName("defaultValue returns value when not null")
        void defaultValueReturnsValue() throws ScriptException {
            var result = evaluate("defaultValue(text, 'default')", "{text: 'hello'}");
            assertThat(result.asString()).isEqualTo("hello");
        }

        @Test
        @DisplayName("defaultValue returns default when null")
        void defaultValueReturnsDefault() throws ScriptException {
            var result = evaluate("defaultValue(text, 'default')", "{text: null}");
            assertThat(result.asString()).isEqualTo("default");
        }

        @Test
        @DisplayName("defaultValue returns default when undefined")
        void defaultValueReturnsDefaultUndefined() throws ScriptException {
            var result = evaluate("defaultValue(text, 'default')", "{}");
            assertThat(result.asString()).isEqualTo("default");
        }

        @Test
        @DisplayName("defaultValue returns default when empty string")
        void defaultValueReturnsDefaultEmpty() throws ScriptException {
            var result = evaluate("defaultValue(text, 'default')", "{text: ''}");
            assertThat(result.asString()).isEqualTo("default");
        }

        @Test
        @DisplayName("defaultValue returns value when zero")
        void defaultValueReturnsZero() throws ScriptException {
            var result = evaluate("defaultValue(number, defaultValue)", "{number: 0, defaultValue: 42}");
            assertThat(result.asInt()).isEqualTo(0);
        }
    }

    private Value evaluate(String expression, String data) throws ScriptException {
        var testScript = (javascript + """
                
                    var data = new Data(${data});
                    var expressionParser = new ExpressionParser(elFunctions);
                    var expression = expressionParser.parse("${expression}");
                    expression.evaluate(data);
                """).replace("${expression}", expression).replace("${data}", data);
        return JSUtil.execute(testScript, BrowserFunctions.BINDINGS);
    }
}
