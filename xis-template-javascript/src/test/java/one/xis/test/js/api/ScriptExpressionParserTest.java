package one.xis.test.js.api;

import one.xis.test.JavaScript;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

class ScriptExpressionParserTest {

    @Test
    void simpleVariable() throws ScriptException {
        JavaScript.builder()
                .withApi()
                .withScript("var data = new Data({test: 123});")
                .withScript("new ScriptExpressionParser('test').tryParse().evaluate(data);")
                .build().assertResultEquals(123);
    }

    @Test
    void objectVariableWithField() throws ScriptException {
        JavaScript.builder()
                .withApi()
                .withScript("var data = new Data({appointment: {location: 'Office'}});")
                .withScript("new ScriptExpressionParser('appointment.location').tryParse().evaluate(data);")
                .build().assertResultEquals("Office");
    }


    @Test
    void variableWithFunction() throws ScriptException {
        JavaScript.builder()
                .withApi()
                .withScript("new ScriptExpressionParser('formatDate(appointment.state)').tryParse().script;")
                .build().assertResultEquals("formatDate(data.getValue(['appointment','state']))");
    }


}
