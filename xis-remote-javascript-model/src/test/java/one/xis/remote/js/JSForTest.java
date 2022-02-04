package one.xis.remote.js;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

class JSForTest {

    private String javascript;

    @BeforeEach
    void init() {
        JSVar result = new JSVar("result");
        JSVar arr = new JSVar("arr");
        JSVarDeclaration arrDeclaration = new JSVarDeclaration(arr, "[1,2,3,4,5]");
        JSVarDeclaration resultDeclaration = new JSVarDeclaration(result, "0");
        JSFor jsFor = new JSFor(arr, "i");
        jsFor.addStatement(new JSCode("result+=arr[i]"));

        javascript = new StringBuilder()
                .append(JSUtil.javascript(arrDeclaration))
                .append(";")
                .append(JSUtil.javascript(resultDeclaration))
                .append(";")
                .append(JSUtil.javascript(jsFor))
                .append(";")
                .append("result;")
                .toString();
    }

    @Test
    void writeJS() throws ScriptException {
        Object result = JSUtil.execute(javascript);
        assertThat(result).isEqualTo(15);
    }
}