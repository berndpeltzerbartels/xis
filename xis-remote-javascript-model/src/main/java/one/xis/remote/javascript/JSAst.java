package one.xis.remote.javascript;

import lombok.Data;

import java.util.*;

@Data
public class JSAst {

    private final List<JSElement> elements = new ArrayList<>();
    private final Collection<JSClass> classes = new ArrayList<>();
    private final Collection<JSFunction> functions = new ArrayList<>();
    private final Collection<JSGlobal> globalVars = new ArrayList<>();
    private final Map<JSGlobal, JSAssignment> assignments = new HashMap<>();

    public JSClass addClass(String name, List<String> constructorParameters) {
        JSClass jsClass = new JSClass(name, constructorParameters);
        elements.add(jsClass);
        return jsClass;
    }

    public JSClass addClass(String name) {
        return addClass(name, Collections.emptyList());
    }

    public JSFunction addFunction(String name, List<String> parameterNames) {
        JSFunction jsFunction = new JSFunction(name, parameterNames);
        elements.add(jsFunction);
        return jsFunction;
    }

    public JSFunction addFunction(String name) {
        return addFunction(name, Collections.emptyList());
    }

    public JSGlobal add(String globalName) {
        JSGlobal global = new JSGlobal(globalName);
        elements.add(global);
        return global;
    }

    public JSAssignment addAssignment(JSGlobal global, String expression) {
        JSAssignment assignment = new JSAssignment(global, expression);
        elements.add(global);
        return assignment;
    }

}
