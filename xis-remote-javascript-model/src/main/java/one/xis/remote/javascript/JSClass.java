package one.xis.remote.javascript;

import lombok.Data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Data
public class JSClass implements JSElement {
    private final String className;
    private final List<String> constructorParameters;
    private final Collection<JSField> fields = new HashSet<>();
    private final Collection<JSFunction> methods = new HashSet<>();

    public JSField addField(String name) {
        JSField field = new JSField(name);
        fields.add(field);
        return field;
    }

    public JSMethod addMethod(JSMethod method) {
        methods.add(method);
        return method;
    }

    public JSMethod addMethod(String methodName, List<String> parameterNames) {
        return addMethod(new JSMethod(methodName, parameterNames));
    }

    public JSMethod addMethod(String methodName) {
        return addMethod(methodName, Collections.emptyList());
    }

}
