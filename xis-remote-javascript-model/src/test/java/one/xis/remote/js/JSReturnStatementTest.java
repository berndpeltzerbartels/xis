package one.xis.remote.js;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

class JSReturnStatementTest {

    private JSFunction jsFunction;

    @BeforeEach
    void setUp() {
        jsFunction = new JSFunction("test");
        JSVar jsVar = new JSVar("i");
        jsFunction.addStatement(new JSVarDeclaration(jsVar, "42"));
        jsFunction.addStatement(new JSReturnStatement(jsVar));
    }

    @Test
    void writeJS() throws ScriptException {
        var js = JSUtil.javascript(jsFunction) + " test();";
        var result = JSUtil.execute(js);

        assertThat(result).isEqualTo(42);
    }
}