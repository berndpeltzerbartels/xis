package one.xis.js;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class JSScriptValidator {

    public void validate(JSScript script) {
        script.getDeclarations().stream()
                .filter(JSClass.class::isInstance)
                .map(JSClass.class::cast)
                .forEach(this::validate);
    }


    private void validate(JSClass jsClass) {
        if (jsClass.getSuperClass() != null) {
            validateAbstractMethodsOverridden(jsClass, jsClass.getSuperClass());
            validateAbstractFields(jsClass, jsClass.getSuperClass());
        }
    }

    private void validateAbstractMethodsOverridden(JSClass jsClass, JSSuperClass superClass) {
        Set<String> methods = new HashSet<>(superClass.getAbstractMethods().keySet());
        methods.removeAll(jsClass.getOverriddenMethods().keySet());
        if (!methods.isEmpty()) {
            throw new JSValidationException("methods must be overridden: " + methods.stream().collect(Collectors.joining(", ")));
        }
    }

    private void validateAbstractFields(JSClass jsClass, JSSuperClass superClass) {
        Set<String> fields = new HashSet<>(superClass.getAbstractFields());
        fields.removeAll(jsClass.getFields().keySet());
        if (!fields.isEmpty()) {
            throw new JSValidationException("fields must be set: " + fields.stream().collect(Collectors.joining(", ")));
        }
    }
}
