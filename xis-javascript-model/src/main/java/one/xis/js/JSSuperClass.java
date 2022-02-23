package one.xis.js;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;


@Getter
public class JSSuperClass extends JSClass {
    private final Map<String, JSMethod> methods = new HashMap<>();
    private final Map<String, JSMethod> abstractMethods = new HashMap<>();

    public JSSuperClass(String className) {
        super(className);
    }

    public JSSuperClass addMethod(String name, int args) {
        methods.put(name, new JSMethod(this, name, args));
        return this;
    }

    public JSSuperClass addAbstractMethod(String name) {
        // We are using abstract methods, without parematers only
        abstractMethods.put(name, new JSMethod(this, name, 0));
        return this;
    }


    @Override
    public JSMethod getMethod(String name) {
        JSMethod method = methods.get(name);
        if (method == null) {
            throw new NoSuchMethodError(name);
        }
        return method;
    }


    @Override
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


    public Map<String, JSMethod> getAllMethods() {
        Map<String, JSMethod> methodMap = new HashMap<>();
        methodMap.putAll(methods);
        methodMap.putAll(abstractMethods);
        return methodMap;
    }

}
