package one.xis.js;

import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class JavascriptAbstractClassesTest {

    private final String PARAM_PATTERN = "[\\S^,]+";


    @Test
    void templateObject() {
        checkClass(JavascriptAbstractClasses.XIS_TEMPLATE_OBJECT);
    }

    @Test
    void component() {
        checkClass(JavascriptAbstractClasses.XIS_COMPONENT);
    }

    @Test
    void widget() {
        checkClass(JavascriptAbstractClasses.XIS_WIDGET);
    }

    @Test
    void page() {
        checkClass(JavascriptAbstractClasses.XIS_PAGE);
    }

    @Test
    void container() {
        checkClass(JavascriptAbstractClasses.XIS_CONTAINER);
    }

    @Test
    void element() {
        checkClass(JavascriptAbstractClasses.XIS_ELEMENT);
    }

    @Test
    void headElement() {
        checkClass(JavascriptAbstractClasses.XIS_HEAD_ELEMENT);
    }

    @Test
    void bodyElement() {
        checkClass(JavascriptAbstractClasses.XIS_BODY_ELEMENT);
    }

    @Test
    void valueHolder() {
        checkClass(JavascriptAbstractClasses.XIS_VALUE_HOLDER);
    }

    @Test
    void staticTextNode() {
        checkClass(JavascriptAbstractClasses.XIS_STATIC_TEXT_NODE);
    }

    @Test
    void mutableTextNode() {
        checkClass(JavascriptAbstractClasses.XIS_MUTABLE_TEXT_NODE);
    }

    @Test
    void ifOperator() {
        // TODO checkClass(JavascriptAbstractClasses.XIS_IF);
    }

    @Test
    void loop() {
        // TODO checkClass(JavascriptAbstractClasses.XIS_LOOP);
    }


    private void checkClass(JSAbstractClass superClass) {
        checkClass(superClass, IOUtils.getResourceAsString("api/component/" + superClass.getClassName() + ".js"));
    }

    private void checkClass(JSAbstractClass superClass, String script) {
        checkClassDeclaration(superClass, script);
        checkDeclaredMethods(superClass, script);
        checkAbstractMethods(superClass, script);
        if (superClass.getSuperClass() != null) {
            checkSuperClass(superClass.getSuperClass().getClassName(), script);
        }
    }

    private void checkSuperClass(String superClassname, String script) {
        if (script.matches("extends\\s+" + superClassname)) {
            throw new AssertionFailedError("not found: extends  " + superClassname);
        }
    }

    private void checkClassDeclaration(JSAbstractClass superClass, String script) {
        if (!classDeclarationFound(superClass, script)) {
            throw new AssertionFailedError("class declaration not found: " + superClass.getClassName());
        }
    }

    private boolean classDeclarationFound(JSAbstractClass superClass, String script) {
        return Pattern.compile(".*class\\s" + superClass.getClassName() + ".*").matcher(script).find();
    }

    private void checkDeclaredMethods(JSAbstractClass superClass, String script) {
        superClass.getDeclaredMethods().values()
                .forEach(method -> checkDeclaredMethod(method.getName(), method.getArgs().size(), script));
    }

    private void checkAbstractMethods(JSAbstractClass superClass, String script) {
        superClass.getDeclaredAbstractMethods().values()
                .forEach(method -> checkAbstractMethod(method.getName(), method.getArgs().size(), script));
    }


    private void checkDeclaredMethod(String methodName, int args, String script) {
        if (!containsDeclaredMethod(methodName, args, script)) {
            throw new AssertionFailedError("method not found or number of args wrong: " + methodName);
        }
    }

    private void checkAbstractMethod(String methodName, int args, String script) {
        if (!containsAbstractMethod(methodName, args, script)) {
            throw new AssertionFailedError("abstract method not found or number of args wrong: " + methodName);
        }
    }

    private boolean containsDeclaredMethod(String methodName, int args, String script) {
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



