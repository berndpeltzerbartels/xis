package one.xis.remote.js;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

class JSFieldAssigmentTest {
    
    private JSObjectInstance objectInstance;

    @BeforeEach
    void init() {
        objectInstance = new JSObjectInstance("o");
        objectInstance.addStringField("field", "123");
    }

    @Test
    void writeJS() throws ScriptException {
        String js = JSUtil.javascript(objectInstance) + "o.field";

        Object result = JSUtil.execute(js);

        assertThat(result).isEqualTo("123");


    }
}