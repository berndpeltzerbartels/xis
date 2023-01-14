package one.xis.test.js.api;

import one.xis.test.JavaScript;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

class ScriptExpressionTest {

    @Test
    void simpleVariable() throws ScriptException {
        JavaScript.builder()
                .withApi()
                .withScript("var data = new Data({test: 123});")
                .withScript("new ScriptExpression('${test}').evaluate(data);")
                .build().assertResultEquals(123);
    }

    @Test
    void objectVariableWithField() throws ScriptException {
        JavaScript.builder()
                .withApi()
                .withScript("var data = new Data({appointment: {location: 'Office'}});")
                .withScript("new ScriptExpression('${appointment.location}').evaluate(data);")
                .build().assertResultEquals("Office");
    }

    @Test
    void textWithVariable() throws ScriptException {
        JavaScript.builder()
                .withApi()
                .withScript("var data = new Data({saint: 'Nikolaus'});")
                .withScript("new ScriptExpression('Das ist das Haus vom ${saint}').evaluate(data);")
                .build().assertResultEquals("Das ist das Haus vom Nikolaus");
    }

    @Test
    void variableWithFunction() throws ScriptException {
        JavaScript.builder()
                .withApi()
                .withScript("new ScriptExpression('${formatDate(appointment.state)').script;")
                .build().assertResultEquals("formatDate(data.getValue());");
    }


}
