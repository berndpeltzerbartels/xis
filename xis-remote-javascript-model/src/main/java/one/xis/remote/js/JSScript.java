package one.xis.remote.js;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JSScript implements JSElement {
    private final List<JSFunction> functions = new ArrayList<>();
    private final List<JSObject> objectInstances = new ArrayList<>();

    public JSFunction addFunction(String functionName, String... parameterNames) {
        return addFunction(new JSFunction(functionName, Arrays.stream(parameterNames).map(JSParameter::new).collect(Collectors.toList())));
    }

    public JSFunction addFunction(JSFunction jsFunction) {
        functions.add(jsFunction);
        return jsFunction;
    }

    public JSObject addObject(String name) {
        return addObject(new JSObject(name));
    }


    public JSObject addObject(JSObject jsObjectInstance) {
        objectInstances.add(jsObjectInstance);
        return jsObjectInstance;
    }

    @Override
    public void writeJS(PrintWriter writer) {
        functions.forEach(funct -> funct.writeJS(writer));
        objectInstances.forEach(jsObjectInstance -> jsObjectInstance.writeJS(writer));
    }


    public Optional<JSFunction> getFunctionByName(String name) {
        return functions.stream().filter(fct -> fct.getName().equals(name)).findFirst();
    }
}
