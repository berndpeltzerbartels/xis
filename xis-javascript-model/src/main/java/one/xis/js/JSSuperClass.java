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
    private final Map<String, JSField> declaredAbstractFields = new HashMap<>();
    private final Map<String, JSField> abstractFields = new HashMap<>();
    private final Map<String, JSField> fields = new HashMap<>();
    private final String className;
    private final String[] constructorArgs;

    public JSSuperClass(String className, String... constructorArgs) {
        super(className, constructorArgs);
        this.className = className;
        this.constructorArgs = constructorArgs;
    }


    public JSSuperClass superClass(JSSuperClass superClass) {
        abstractMethods.putAll(superClass.getAbstractMethods());
        methods.putAll(superClass.getMethods());
        methods.keySet().forEach(abstractMethods::remove);
        abstractFields.putAll(superClass.getAbstractFields());
        fields.putAll(superClass.getFields());
        fields.keySet().forEach(abstractFields::remove);
        return this;
    }


    public JSSuperClass addDeclaredMethod(String name, int args) {
        var method = new JSMethod(this, name, unspecifiedArgNames(args)); // TODO remove unspecified ?
        methods.put(name, method);
        declaredMethods.put(name, method);
        return this;
    }

    public JSSuperClass addDeclaredMethod(String name) {
        return addDeclaredMethod(name, 0);
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

    public JSSuperClass addAbstractField(String name) {
        var field = new JSField(this, name);
        this.abstractFields.put(name, field);
        this.declaredAbstractFields.put(name, field);
        return this;
    }

    @Override
    public JSMethod getMethod(String name) {
        var method = methods.get(name);
        if (method == null) {
            throw new NoSuchJavascriptMethodError(name);
        }
        return method;
    }


    @Override
    public JSMethod overrideAbstractMethod(String name) {
        var method = abstractMethods.get(name);
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
