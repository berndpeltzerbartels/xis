package one.xis.js;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
public class JSSuperClass extends JSClass {
    private final Map<String, JSMethod> declaredMethods = new HashMap<>();
    private final Map<String, JSMethod> declaredAbstractMethods = new HashMap<>();
    private final Map<String, JSMethod> methods = new HashMap<>();
    private final Map<String, JSMethod> abstractMethods = new HashMap<>();

    public JSSuperClass(String className, String... constructorArgs) {
        super(className, constructorArgs);
    }

    public JSSuperClass(String className, JSSuperClass superClass, String... constructorArgs) {
        super(className, constructorArgs);
        abstractMethods.putAll(superClass.getAbstractMethods());
        methods.putAll(superClass.getMethods());
        methods.forEach(abstractMethods::remove);
    }

    public JSSuperClass addMethod(String name, int args) {
        var method = new JSMethod(this, name, unspecifiedArgNames(args)); // TODO remove unspecified ?
        methods.put(name, method);
        declaredMethods.put(name, method);
        return this;
    }

    public JSSuperClass addMethod(String name) {
        return addMethod(name, 0);
    }

    public JSSuperClass addAbstractMethod(String name) {
        return addAbstractMethod(name, 0);
    }

    public JSSuperClass addAbstractMethod(String name, int args) {
        // We are using abstract methods, without parematers only
        var method = new JSMethod(this, name, unspecifiedArgNames(args));
        abstractMethods.put(name, method);
        declaredAbstractMethods.put(name, method);
        return this;
    }

    @Override
    public JSMethod getMethod(String name) {
        JSMethod method = methods.get(name);
        if (method == null) {
            throw new NoSuchJavascriptMethodError(name);
        }
        return method;
    }


    @Override
    public JSMethod overrideAbstractMethod(String name) {
        JSMethod method = abstractMethods.get(name);
        if (method == null) {
            method = methods.get(name);
        }
        if (method == null) {
            throw new NoSuchJavascriptMethodError(name);
        }
        return method;
    }


    public Map<String, JSMethod> getAllMethods() {
        Map<String, JSMethod> methodMap = new HashMap<>();
        methodMap.putAll(methods);
        methodMap.putAll(abstractMethods);
        return methodMap;
    }


    private List<String> unspecifiedArgNames(int count) {
        int index = 1;
        List<String> args = new ArrayList<>();
        while (args.size() < count) {
            args.add("arg" + index++);
        }
        return args;
    }

}
