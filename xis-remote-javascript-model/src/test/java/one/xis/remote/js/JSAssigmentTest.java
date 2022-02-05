package one.xis.remote.js;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

class JSAssigmentTest {

    private JSObject objectInstance;


    @BeforeEach
    void init() {
        objectInstance = new JSObject("o");
        JSField field = objectInstance.addField("field", "123");
        objectInstance.addMethod("change").addStatement(new JSAssigment(field, "456"));
    }

    @Test
    void writeJS() throws ScriptException {
        String js = JSUtil.javascript(objectInstance) + "o.change();o.field";
        Object result = JSUtil.execute(js);

        assertThat(result).isEqualTo("456");
    }
}