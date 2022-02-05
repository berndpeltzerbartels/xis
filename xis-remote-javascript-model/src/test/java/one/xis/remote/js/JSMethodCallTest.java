package one.xis.remote.js;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

class JSMethodCallTest {

    private JSObject objectInstance;
    private JSMethod method;

    @BeforeEach
    void init() {
        objectInstance = new JSObject("o");
        objectInstance.addField("field", "123");
        method = objectInstance.addMethod("up");
        method.addStatement(new JSCode("this.field++"));
    }

    @Test
    void writeJS() throws ScriptException {
        String js = JSUtil.javascript(objectInstance) + JSUtil.javascript(new JSMethodCall(objectInstance, method)) + ";o.field";
        Object result = JSUtil.execute(js);

        assertThat(result).isEqualTo(124);
    }
}