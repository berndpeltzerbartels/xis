package one.xis.js;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class JSScriptValidator {
    // TODO use it
    public void validate(JSScript script) {
        script.getClassDeclarations().stream()
                .filter(JSClass.class::isInstance)
                .map(JSClass.class::cast)
                .forEach(this::validate);
    }


    private void validate(JSClass jsClass) {
        if (jsClass.getSuperClass() != null) {
            validateAbstractMethodsOverridden(jsClass, jsClass.getSuperClass());
        }
    }

    private void validateAbstractMethodsOverridden(JSClass jsClass, JSSuperClass superClass) {
        Set<String> methods = new HashSet<>(superClass.getAbstractMethods().keySet());
        methods.removeAll(jsClass.getOverriddenMethods().keySet());
        if (!methods.isEmpty()) {
            throw new JSValidationException("one or more methods are unimplemented: " + methods.stream().collect(Collectors.joining(", ")));
        }
    }


}
