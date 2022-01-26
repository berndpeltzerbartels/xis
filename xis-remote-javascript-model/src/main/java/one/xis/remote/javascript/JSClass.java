package one.xis.remote.javascript;

import lombok.Data;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Data
public class JSClass {
    private final String className;
    private final List<String> constructorParameters;
    private final Collection<JSField> fields = new HashSet<>();
    private final Collection<JSFunction> methods = new HashSet<>();

    public JSField addField(String name) {
        JSField field = new JSField(name, this);
        fields.add(field);
        return field;
    }

    public JSMethod addMethod(JSMethod method) {
        methods.add(method);
        return method;
    }


}
