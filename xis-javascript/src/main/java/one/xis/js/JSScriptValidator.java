package one.xis.js;

import java.util.HashSet;
import java.util.Set;

public class JSScriptValidator {

    public static void validate(JSScript script) {
        validateOveridingMethodsComplete(script);
        validateOveridingFieldsComplete(script);
    }

    private static void validateOveridingMethodsComplete(JSScript script) {
        script.getClassDeclarations().forEach(JSScriptValidator::validateOveridingMethodsComplete);
    }

    private static void validateOveridingFieldsComplete(JSScript script) {
        script.getClassDeclarations().forEach(JSScriptValidator::validateOveridingFieldsComplete);
    }


    private static void validateOveridingMethodsComplete(JSClass jsClass) {
        if (jsClass.getSuperClass() != null) {
            validateAbstractMethodsOverridden(jsClass, jsClass.getSuperClass());
        }
    }

    private static void validateOveridingFieldsComplete(JSClass jsClass) {
        if (jsClass.getSuperClass() != null) {
            validateAbstractFielddsOverridden(jsClass, jsClass.getSuperClass());
        }
    }

    private static void validateAbstractMethodsOverridden(JSClass jsClass, JSSuperClass superClass) {
        Set<String> methods = new HashSet<>(superClass.getAbstractMethods().keySet());
        methods.removeAll(jsClass.getOverriddenMethods().keySet());
        if (!methods.isEmpty()) {
            throw new JSValidationException("one or more methods are unimplemented in subclass of " + superClass.getClassName() + ": " + String.join(", ", methods));
        }
    }

    private static void validateAbstractFielddsOverridden(JSClass jsClass, JSSuperClass superClass) {
        Set<String> methods = new HashSet<>(superClass.getAbstractMethods().keySet());
        methods.removeAll(jsClass.getOverriddenMethods().keySet());
        if (!methods.isEmpty()) {
            throw new JSValidationException("one or more methods are unimplemented in subclass of " + superClass.getClassName() + ": " + String.join(", ", methods));
        }
    }


}
