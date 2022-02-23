package one.xis.js;

import lombok.Data;
import lombok.experimental.NonFinal;

import java.util.HashMap;
import java.util.Map;

@Data
@NonFinal
public class JSClass implements JSDeclaration, JSContext {
    private final String className;
    private JSSuperClass superClass;
    private final Map<String, JSField> fields = new HashMap<>();
    private final Map<String, JSMethod> overriddenMethods = new HashMap<>();

    public JSClass(String className) {
        this.className = className;
        this.superClass = null;
    }

    public JSClass(String className, JSSuperClass superClass) {
        this.className = className;
        this.superClass = superClass;
    }

    public JSMethod getMethod(String name) {
        JSMethod method = overriddenMethods.get(name);
        if (method == null) {
            method = superClass.getAbstractMethods().get(name);
        }
        if (method == null) {
            method = superClass.getMethod(name);
        }
        if (method == null) {
            throw new NoSuchJavascriptMethodError(className + "#" + name);
        }
        return method;
    }


    public JSMethod overrideMethod(String name) {
        JSMethod method = superClass.getAbstractMethods().get(name);
        if (method == null) {
            method = superClass.getMethod(name);
        }
        if (method == null) {
            throw new NoSuchJavascriptMethodError(superClass.getClassName() + "#" + name);
        }
        JSMethod overriddenMethod = new JSMethod(this, name, method.getArgs());
        overriddenMethods.put(name, overriddenMethod);
        return overriddenMethod;
    }


    public JSClass derrivedFrom(JSSuperClass jsClass) {
        superClass = jsClass;
        return this;
    }

    public JSClass addField(String name, JSValue value) {
        JSField field = new JSField(this, name);
        field.setValue(value);
        fields.put(name, field);
        return this;
    }

    public JSField getField(String name) {
        return fields.get(name);
    }
}
