package one.xis.js;

import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class JavascriptSuperClassesTest {

    private final String PARAM_PATTERN = "[\\S^,]+";


    @Test
    void templateObject() {
        checkClass(JavascriptSuperClasses.XIS_TEMPLATE_OBJECT);
    }

    @Test
    void component() {
        checkClass(JavascriptSuperClasses.XIS_COMPONENT);
    }

    @Test
    void widget() {
        checkClass(JavascriptSuperClasses.XIS_WIDGET);
    }

    private void checkClass(JSSuperClass superClass) {
        checkClass(superClass, IOUtils.getResourceAsString("api/component/" + superClass.getClassName() + ".js"));
    }

    private void checkClass(JSSuperClass superClass, String script) {
        checkClassDeclaration(superClass, script);
        checkMethods(superClass, script);
        checkAbstractMethods(superClass, script);
    }


    private void checkClassDeclaration(JSSuperClass superClass, String script) {
        if (!classDeclarationFound(superClass, script)) {
            throw new AssertionFailedError("class declaration not found: " + superClass.getClassName());
        }
    }

    private boolean classDeclarationFound(JSSuperClass superClass, String script) {
        return Pattern.compile(".*class\\s" + superClass.getClassName() + ".*").matcher(script).find();
    }

    private void checkMethods(JSSuperClass superClass, String script) {
        superClass.getDeclaredMethods().values()
                .forEach(method -> checkMethod(method.getName(), method.getArgs().size(), script));
    }

    private void checkAbstractMethods(JSSuperClass superClass, String script) {
        superClass.getDeclaredAbstractMethods().values()
                .forEach(method -> checkAbstractMethod(method.getName(), method.getArgs().size(), script));
    }


    private void checkMethod(String methodName, int args, String script) {
        if (!containsMethod(methodName, args, script)) {
            throw new AssertionFailedError("method not found or number of args wrong: " + methodName);
        }
    }

    private void checkAbstractMethod(String methodName, int args, String script) {
        if (!containsAbstractMethod(methodName, args, script)) {
            throw new AssertionFailedError("abstract method not found or number of args wrong: " + methodName);
        }
    }

    private boolean containsMethod(String methodName, int args, String script) {
        return patternForMethod(methodName, args).matcher(script).find();
    }

    private boolean containsAbstractMethod(String methodName, int args, String script) {
        return patternForAbstractMethod(methodName, args).matcher(script).find();

    }

    private Pattern patternForMethod(String methodName, int args) {
        return Pattern.compile(".*" + methodName + "\\(\\s*" + argPattern(args) + "\\s*\\)\\s*\\{.*", Pattern.MULTILINE);
    }


    private Pattern patternForAbstractMethod(String methodName, int args) {
        return Pattern.compile(".*" + methodName + "\\(\\s*" + argPattern(args) + "\\s*\\)\\s*\\{(\\s+throw\\s+new\\s+Error\\(['\"]abstract.*)", Pattern.MULTILINE);
    }


    private String argPattern(int numOfArgs) {
        return IntStream.iterate(0, i -> i < numOfArgs, i -> i + 1)
                .mapToObj(i -> PARAM_PATTERN)
                .collect(Collectors.joining("\\s*,\\s*"));
    }


}



