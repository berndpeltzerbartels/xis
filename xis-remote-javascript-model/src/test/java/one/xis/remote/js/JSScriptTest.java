package one.xis.remote.js;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

class JSScriptTest {

    private static final String INSTANCE_NAME = "instance";
    private static final String FIELD_NAME = "field";
    private static final String FIELD_DEFAULT_VALUE = "123";
    private static final String METHOD_NAME = "test";
    private static final String METHOD_PARAM_NAME1 = "param1";
    private static final String METHOD_PARAM_NAME2 = "param2";


    private JSFunction jsFunction;
    private JSObjectInstance objectInstance;

    @BeforeEach
    void createFunction() {
        jsFunction = new JSFunction("add", "p1", "p2");
        JSParameter p1 = jsFunction.getParameters().get(0);
        JSParameter p2 = jsFunction.getParameters().get(1);
        JSVar rv = new JSVar("rv");
        jsFunction.addStatement(new JSVarDeclaration(rv, p1));
        jsFunction.addStatement(new JSCode("rv+=" + p2.getName()));
        jsFunction.addStatement(new JSReturnStatement(rv));
    }

    @BeforeEach
    void createObjectInstance() {
        objectInstance = new JSObjectInstance(INSTANCE_NAME);
        var field = objectInstance.addField(FIELD_NAME, FIELD_DEFAULT_VALUE);
        var method = objectInstance.addMethod(METHOD_NAME, METHOD_PARAM_NAME1, METHOD_PARAM_NAME2);
        method.addStatement(new JSFieldAssigment(field, method.getParameters().get(0)));
        var rv = new JSVar("rv");
        method.addStatement(new JSVarDeclaration(rv, METHOD_PARAM_NAME1 + "+" + METHOD_PARAM_NAME2));
        method.addStatement(new JSReturnStatement(rv));
    }

    @Test
    void writeJS() throws ScriptException {
        var script = new JSScript();
        script.addObjectInstance(objectInstance);
        script.addFunction(jsFunction);

        var js = JSUtil.javascript(script) + ";add(instance.test(3,7), 6);";

        var result = JSUtil.execute(js);

        assertThat(result).isEqualTo(16);


    }

}