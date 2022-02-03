package one.xis.remote.js;

import one.xis.utils.js.JSUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class JSFunctionTest {
    private JSFunction jsFunction;

    @BeforeEach
    void init() {
        jsFunction = new JSFunction("add", "p1", "p2");
        JSParameter p1 = jsFunction.getParameters().get(0);
        JSParameter p2 = jsFunction.getParameters().get(1);
        JSVar rv = new JSVar("rv");
        jsFunction.addStatement(new JSVarDeclaration(rv, p1));
        jsFunction.addStatement(new JSCode("rv+=" + p2.getName()));
        jsFunction.setReturnValue(rv);
    }


    @Test
    void writeJs() throws ScriptException {
        StringWriter writer = new StringWriter();
        jsFunction.writeJS(new PrintWriter(writer));
        String js = writer + ";add(5, 3);";

        // We call the function
        Object result = JSUtil.compile(js).eval();

        assertThat(result).isEqualTo(8);
    }
}
