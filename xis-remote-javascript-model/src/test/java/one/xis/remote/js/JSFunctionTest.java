package one.xis.remote.js;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

public class JSFunctionTest {
    private JSFunction jsFunction;

    @BeforeEach
    void init() {
        jsFunction = new JSFunction("add", "p1", "p2");
        var p1 = jsFunction.getParameters().get(0);
        var p2 = jsFunction.getParameters().get(1);
        var rv = new JSVar("rv");
        jsFunction.addStatement(new JSVarDeclaration(rv, p1));
        jsFunction.addStatement(new JSCode("rv+=" + p2.getName()));
        jsFunction.addStatement(new JSReturnStatement(rv));
    }


    @Test
    void writeJs() throws ScriptException {
        var js = JSUtil.javascript(jsFunction) + ";add(5, 3);";

        // We call the function
        var result = JSUtil.execute(js);

        assertThat(result).isEqualTo(8);
    }
}
