package one.xis.remote.javascript;

import java.util.List;

public class JSMethod extends JSFunction {

    public JSMethod(String name, List<String> parameters) {
        super(name, parameters);
    }

    void setReturnField(JSField field) {
        this.returnVar = field;
    }


}
