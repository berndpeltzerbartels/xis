package one.xis.remote.javascript;

import java.util.List;

public class JSMethod extends JSFunction {
    private final JSClass jsClass;

    public JSMethod(String name, List<JSVar> parameters, JSClass jsClass) {
        super(name, parameters);
        this.jsClass = jsClass;
    }

    void setReturnField(String varName) {
        this.returnValue = jsClass.getFields().stream().filter(f -> f.getName().equals(varName)).findFirst().orElseThrow(() -> new IllegalStateException("invalid return field: " + varName));
    }


}
