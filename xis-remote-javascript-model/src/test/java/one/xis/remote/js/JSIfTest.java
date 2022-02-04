package one.xis.remote.js;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

class JSIfTest {


    private String javascript;

    @BeforeEach
    void init() {
        JSVar booleanVar = new JSVar("b");
        JSVar numberVar = new JSVar("i");
        JSVarDeclaration booleanDeclaration = new JSVarDeclaration(booleanVar, "true");
        JSVarDeclaration numberDeclaration = new JSVarDeclaration(booleanVar, "1");
        JSIf jsIf = new JSIf(booleanVar);
        jsIf.addStatement(new JSAssigment(numberVar, "2"));

        javascript = new StringBuilder()
                .append(JSUtil.javascript(booleanDeclaration))
                .append(";")
                .append(JSUtil.javascript(numberDeclaration))
                .append(";")
                .append(JSUtil.javascript(jsIf))
                .append(";")
                .append("i;")
                .toString();
    }

    @Test
    void writeJS() throws ScriptException {
        Object result = JSUtil.execute(javascript);
        assertThat(result).isEqualTo(2);
    }
}
