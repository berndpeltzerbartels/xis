package one.xis.test.js.api;

import one.xis.test.JavaScript;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

@Disabled
class TextWithVarsParserTest {

    @Test
    void textWithVariable() throws ScriptException {
        JavaScript.builder()
                .withApi()
                .withScript("var data = new Data({saint: 'Nikolaus'});")
                .withScript("new TextWithVarsParser('Das ist das Haus vom ${saint}').parse().evaluate(data);")
                .build().assertResultEquals("Das ist das Haus vom Nikolaus");
    }
}
