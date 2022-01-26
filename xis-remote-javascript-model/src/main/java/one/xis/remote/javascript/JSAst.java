package one.xis.remote.javascript;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Data
public class JSAst {

    private final Collection<JSClass> classes = new ArrayList<>();
    private final Collection<JSFunction> functions = new ArrayList<>();
    private final Collection<JSGlobal> globalVars = new ArrayList<>();
    private final Map<JSGlobal, JSAssignment> assignments = new HashMap<>();

    public JSClass add(JSClass jsClass) {
        getClasses().add(jsClass);
        return jsClass;
    }

    public JSFunction add(JSFunction jsFunction) {
        functions.add(jsFunction);
        return jsFunction;
    }

    public JSGlobal add(String globalName) {
        JSGlobal global = new JSGlobal(globalName);
        globalVars.add(global);
        return global;
    }

    public JSAssignment addAssignment(JSGlobal global, String expression) {
        JSAssignment assignment = new JSAssignment(global, expression);
        assignments.put(global, assignment);
        return assignment;
    }

}
