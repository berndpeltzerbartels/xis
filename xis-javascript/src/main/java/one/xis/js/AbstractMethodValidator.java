package one.xis.js;

import java.util.HashSet;
import java.util.Set;

public class AbstractMethodValidator {

    public static void validateOverridingComplete(JSScript script) {
        script.getClassDeclarations().forEach(AbstractMethodValidator::validateOvveridingComplete);
    }


    private static void validateOvveridingComplete(JSClass jsClass) {
        if (jsClass.getSuperClass() != null) {
            validateAbstractMethodsOverridden(jsClass, jsClass.getSuperClass());
        }
    }

    private static void validateAbstractMethodsOverridden(JSClass jsClass, JSSuperClass superClass) {
        Set<String> methods = new HashSet<>(superClass.getAbstractMethods().keySet());
        methods.removeAll(jsClass.getOverriddenMethods().keySet());
        if (!methods.isEmpty()) {
            throw new JSValidationException("one or more methods are unimplemented: " + String.join(", ", methods));
        }
    }


}
