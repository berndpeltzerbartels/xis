package one.xis.js;

import lombok.Getter;

import java.util.*;


@Getter
public class JSSuperClass extends JSClass {
    private final Map<String, JSMethod> methods = new HashMap<>();
    private final Map<String, JSMethod> abstractMethods = new HashMap<>();
    private final Set<String> abstractFields = new HashSet<>();

    public JSSuperClass(String className) {
        super(className);
    }

    public JSSuperClass addMethod(String name, int args) {
        methods.put(name, new JSMethod(this, name, unspecifiedArgNames(args))); // TODO named args ?
        return this;
    }

    public JSSuperClass addAbstractMethod(String name) {
        // We are using abstract methods, without parematers only
        abstractMethods.put(name, new JSMethod(this, name, Collections.emptyList()));
        return this;
    }

    public JSSuperClass addAbstractField(String name) {
        // We are using abstract methods, without parematers only
        abstractFields.add(name);
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
