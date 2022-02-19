package one.xis.js;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class JSClass implements JSDeclaration, JSContext {
    private final String className;
    private JSClass superClass;
    private final Map<String, JSMethod> methods = new HashMap<>();
    private final Map<String, JSMethod> abstractMethods = new HashMap<>();
    private final Map<String, JSField> fields = new HashMap<>();

    public JSClass(String className) {
        this.className = className;
        this.superClass = null;
    }

    public JSClass(String className, JSClass superClass) {
        this.className = className;
        this.superClass = superClass;
    }


    public JSClass addMethod(String name, int args) {
        methods.put(name, new JSMethod(this, name, args));
        return this;
    }

    public JSClass addAbstractMethod(String name, int args) {
        abstractMethods.put(name, new JSMethod(this, name, args));
        return this;
    }


    public JSMethod getMethod(String name) {
        JSMethod method = abstractMethods.get(name);
        if (method == null) {
            throw new NoSuchMethodError(name);
        }
        return method;
    }


    public JSMethod overrideMethod(String name) {
        JSMethod method = abstractMethods.get(name);
        if (method == null) {
            method = methods.get(name);
        }
        if (method == null) {
            throw new NoSuchMethodError(name);
        }
        return method;
    }


    public JSClass derrivedFrom(JSClass jsClass) {
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
