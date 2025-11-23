package one.xis.js.parse;

import one.xis.js.Javascript;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static one.xis.js.JavascriptSource.CLASSES;
import static org.assertj.core.api.Assertions.assertThat;

class TextContentParserTest {

    private String javascript;

    @BeforeEach
    void init() {
        javascript = Javascript.getScript(CLASSES);
        javascript += """
                
                    var app = {
                        sessionStorage: {
                            mapTextContent: function (textContent) {
                
                            }
                        },
                        localStorage: {
                             mapTextContent: function (textContent) {
                
                            }
                        }
                    };
                """;
    }

    @Test
    @DisplayName("Parse text with variables")
    void testParseTextWithVariables() throws ScriptException {
        // JavaScript-Code für den Test
        var testScript = javascript + """
                
                    var src = "Hello, ${name}!";
                    var handler = {}; // Dummy-Handler
                    var parser = new TextContentParser(src, handler);
                    var textContent = parser.parse();
                
                    var data = new Data({ name: "John" });
                    textContent.evaluate(data);
                """;

        // Test ausführen
        var result = JSUtil.execute(testScript);

        // Überprüfen, ob das Ergebnis korrekt ist
        assertThat(result.asString()).isEqualTo("Hello, John!");
    }

    @Test
    @DisplayName("Parse text without variables")
    void testParseTextWithoutVariables() throws ScriptException {
        // JavaScript-Code für den Test
        var testScript = javascript + """
                    var src = "Static text.";
                    var handler = {}; // Dummy-Handler
                    var parser = new TextContentParser(src, handler);
                    var textContent = parser.parse();
                
                    var data = new Data({});
                    textContent.evaluate(data);
                """;

        // Test ausführen
        var result = JSUtil.execute(testScript);

        // Überprüfen, ob das Ergebnis korrekt ist
        assertThat(result.asString()).isEqualTo("Static text.");
    }

    @Test
    @DisplayName("Parse text with multiple variables")
    void testParseTextWithMultipleVariables() throws ScriptException {
        // JavaScript-Code für den Test
        var testScript = javascript + """
                    var src = "Name: ${name}, Age: ${age}";
                    var handler = {}; // Dummy-Handler
                    var parser = new TextContentParser(src, handler);
                    var textContent = parser.parse();
                
                    var data = new Data({ name: "John", age: 30 });
                    textContent.evaluate(data);
                """;

        // Test ausführen
        var result = JSUtil.execute(testScript);

        // Überprüfen, ob das Ergebnis korrekt ist
        assertThat(result.asString()).isEqualTo("Name: John, Age: 30");
    }

    @Test
    @DisplayName("Parse text with escaped variables")
    void testParseTextWithEscapedVariables() throws ScriptException {
        // JavaScript-Code für den Test
        var testScript = javascript + """
                    var src = "This is not a variable: \\${escaped}";
                    var handler = {}; // Dummy-Handler
                    var parser = new TextContentParser(src, handler);
                    var textContent = parser.parse();
                
                    var data = new Data({});
                    textContent.evaluate(data);
                """;

        // Test ausführen
        var result = JSUtil.execute(testScript);

        // Überprüfen, ob das Ergebnis korrekt ist
        assertThat(result.asString()).isEqualTo("This is not a variable: ");
    }

    @Test
    @DisplayName("Parse empty text")
    void testParseEmptyText() throws ScriptException {
        // JavaScript-Code für den Test
        var testScript = javascript + """
                    var src = "";
                    var handler = {}; // Dummy-Handler
                    var parser = new TextContentParser(src, handler);
                    var textContent = parser.parse();
                
                    var data = new Data({});
                    textContent.evaluate(data);
                """;

        // Test ausführen
        var result = JSUtil.execute(testScript);

        // Überprüfen, ob das Ergebnis korrekt ist
        assertThat(result.asString()).isEqualTo("");
    }
}