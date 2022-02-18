package one.xis.js;

import lombok.Value;

@Value
public class JSClass implements JSDeclaration {
    String className;
    JSClass superClass;

    public JSClass(String className) {
        this.className = className;
        this.superClass = null;
    }

    public JSClass(String className, JSClass superClass) {
        this.className = className;
        this.superClass = superClass;
    }

    public static JSClass anonymous() {
        return new JSClass(null, null);
    }

    public JSClass addMethod(String name, int args) {
        return this;
    }

    public JSClass addAbstractMethod(String name, int args) {
        return this;
    }


    public JSMethod getMethod(String name) {
        return null; // TODO throw Exception if not present
    }

    public JSObject createVar(String name) {
        return new JSObject(name, this);
    }

    public JSMethod overrideMethod(String name) {
        return null;
    }

    public JSObject newInstance(JSArg... args) {

        return null;
    }

    public JSClass derrivedFrom(JSClass jsClass) {
        return this;
    }

    public JSMethod overrideMethod(JSMethod method) {
        return method;
    }

    public JSClass addField(String name, String value) {
        return this;
    }

    public JSClass addField(String name, JSValue value) {
        return this;
    }

    public JSField getField(String name) {
        return null;
    }

    @Value
    public static class JSClassBuilder {
        String name;

        public JSClassBuilder derrivedFrom(JSClass jsClass) {
            return this;
        }

        public JSClassBuilder overrideMethod(JSMethod method) {
            return this;
        }

        public JSClassBuilder addField(String name, String value) {
            return this;
        }

        public JSClassBuilder addField(String name, JSObject value) {
            return this;
        }

        public JSObject build() {
            return null;
        }

    }


}
