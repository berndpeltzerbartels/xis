package one.xis.remote.js;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

public class JSObjectInstanceTest {

    private static final String INSTANCE_NAME = "instance";
    private static final String FIELD_NAME = "field";
    private static final String FIELD_DEFAULT_VALUE = "123";
    private static final String METHOD_NAME = "test";
    private static final String METHOD_PARAM_NAME1 = "param1";
    private static final String METHOD_PARAM_NAME2 = "param2";

    private static final int PARAM1 = 3;
    private static final int PARAM2 = 5;
    private static final String CALL_TEST_METHOD = INSTANCE_NAME + "." + METHOD_NAME + "(" + PARAM1 + "," + PARAM2 + ");";

    private JSObjectInstance objectInstance;

    @BeforeEach
    void init() {
        objectInstance = new JSObjectInstance(INSTANCE_NAME);
        var field = objectInstance.addField(FIELD_NAME, FIELD_DEFAULT_VALUE);
        var method = objectInstance.addMethod(METHOD_NAME, METHOD_PARAM_NAME1, METHOD_PARAM_NAME2);
        method.addStatement(new JSFieldAssigment(field, method.getParameters().get(0)));
        var rv = new JSVar("rv");
        method.addStatement(new JSVarDeclaration(rv, METHOD_PARAM_NAME1 + "+" + METHOD_PARAM_NAME2));
        method.addStatement(new JSReturnStatement(rv));
    }


    @Test
    void writeJs() throws ScriptException {
        String js = JSUtil.javascript(objectInstance) + CALL_TEST_METHOD;

        // We call the method
        var result = JSUtil.execute(js);

        assertThat(result).isEqualTo(8);
    }
}
