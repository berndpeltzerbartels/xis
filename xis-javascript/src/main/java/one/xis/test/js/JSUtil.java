package one.xis.test.js;

import lombok.experimental.UtilityClass;

import javax.script.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class JSUtil {

    public CompiledScript compile(String javascript) throws ScriptException {
        return compile(javascript, null);
    }

    public CompiledScript compile(String javascript, Map<String, Object> bindingMap) throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("graal.js");
        var bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostClassLookup", true);
        bindings.put("polyglot.js.allowHostAccess", true);
        bindings.putAll(bindingMap);
        Compilable compiler = (Compilable) engine;
        return compiler.compile(javascript);
    }


    public Object execute(String js) throws ScriptException {
        return compile(js).eval();
    }


    private void validateOverridingComplete(JSScript script) {
        script.getClassDeclarations().forEach(JSUtil::validateOvveridingComplete);
    }


    private void validateOvveridingComplete(JSClass jsClass) {
        if (jsClass.getSuperClass() != null) {
            validateAbstractMethodsOverridden(jsClass, jsClass.getSuperClass());
        }
    }

    private void validateAbstractMethodsOverridden(JSClass jsClass, JSSuperClass superClass) {
        Set<String> methods = new HashSet<>(superClass.getAbstractMethods().keySet());
        methods.removeAll(jsClass.getOverriddenMethods().keySet());
        if (!methods.isEmpty()) {
            throw new JSValidationException(getErrorMessageMissingOverride(superClass, methods));
        }
    }


    private String getErrorMessageMissingOverride(JSSuperClass superClass, Set<String> notOverrideMethods) {
        return String.format("Subclasses of %s do not override %s", superClass.getClassName(), String.join(", ", notOverrideMethods));
    }

}
