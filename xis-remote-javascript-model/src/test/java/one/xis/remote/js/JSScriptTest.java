package one.xis.remote.js;

import one.xis.utils.js.JSUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.io.PrintWriter;
import java.io.StringWriter;

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
        jsFunction.setReturnValue(rv);
    }

    @BeforeEach
    void createObjectInstance() {
        objectInstance = new JSObjectInstance(INSTANCE_NAME);
        JSField field = objectInstance.addField(FIELD_NAME, FIELD_DEFAULT_VALUE);
        JSMethod method = objectInstance.addMethod(METHOD_NAME, METHOD_PARAM_NAME1, METHOD_PARAM_NAME2);
        method.addStatement(new JSFieldAssigment(field, method.getParameters().get(0)));
        JSVar rv = new JSVar("rv");
        method.addStatement(new JSVarDeclaration(rv, METHOD_PARAM_NAME1 + "+" + METHOD_PARAM_NAME2));
        method.setReturnValue(rv);

    }

    @Test
    void writeJS() throws ScriptException {
        JSScript script = new JSScript();
        script.addObjectInstance(objectInstance);
        script.addFunction(jsFunction);

        StringWriter writer = new StringWriter();
        script.writeJS(new PrintWriter(writer));
        String js = writer + ";add(instance.test(3,7), 6);";

        Object result = JSUtil.compile(js).eval();

        assertThat(result).isEqualTo(16);


    }

}