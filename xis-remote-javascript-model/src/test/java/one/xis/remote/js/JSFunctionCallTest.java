package one.xis.remote.js;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

class JSFunctionCallTest {

    private JSFunction jsFunction;

    @BeforeEach
    void init() {
        jsFunction = new JSFunction("test", "p1", "p2");
        JSVar result = new JSVar("rv");
        jsFunction.addStatement(new JSVarDeclaration(result, "p1+p2"));
        jsFunction.addStatement(new JSReturnStatement(result));
    }

    @Test
    void writeJS() throws ScriptException {
        String js = JSUtil.javascript(jsFunction) + " test(1,1)";
        Object result = JSUtil.execute(js);

        assertThat(result).isEqualTo(2);
    }
}