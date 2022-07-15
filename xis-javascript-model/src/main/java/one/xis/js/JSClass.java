package one.xis.js;

import lombok.Data;
import lombok.experimental.NonFinal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
@NonFinal
public class JSClass implements JSDeclaration, JSContext {
    private final String className;
    private JSSuperClass superClass;
    private final Map<String, JSField> fields = new HashMap<>();
    private final Map<String, JSMethod> overriddenMethods = new HashMap<>();
    private final JSConstructor constructor;

    public JSClass(String className, String... args) {
        this.className = className;
        this.superClass = null;
        this.constructor = new JSConstructor(args);
    }

    public JSClass(String className, JSSuperClass superClass) {
        this.className = className;
        this.superClass = superClass;
        this.constructor = new JSConstructor();
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


    public JSMethod overrideAbstractMethod(String name) {
        JSMethod method = superClass.getAbstractMethods().get(name);
        if (method == null) {
            throw new IllegalStateException("no abstract method with name: " + name);
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
        if (fields.containsKey(name)) {
            throw new IllegalStateException("field already exists: " + name);
        }
        JSField field = new JSField(this, name);
        field.setValue(value);
        fields.put(name, field);
        return this;
    }

    public JSField getField(String name) {
        return fields.get(name);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append("JSClass{className=\"")
                .append(className).append("\"");
        if (superClass != null) {
            sb.append(", superClass=\"").append(superClass.getClassName()).append("\"");
        }
        return sb.append('}').toString();
    }

    // TODO remove equals and hashcode, but check Lombok causes StackOverflowException (rekursive call)
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JSClass jsClass = (JSClass) o;
        return Objects.equals(className, jsClass.className) && Objects.equals(superClass, jsClass.superClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, superClass);
    }
}
