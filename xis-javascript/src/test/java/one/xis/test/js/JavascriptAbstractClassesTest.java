package one.xis.test.js;

import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class JavascriptAbstractClassesTest {

    private final String PARAM_PATTERN = "[\\S^,]+";


    @Test
    @DisplayName("XISTemplateObject")
    void templateObject() {
        checkClass(JavascriptAbstractClasses.XIS_TEMPLATE_OBJECT);
    }

    @Test
    @DisplayName("XISComponent")
    void component() {
        checkClass(JavascriptAbstractClasses.XIS_COMPONENT);
    }

    @Test
    @DisplayName("XISWidget")
    void widget() {
        checkClass(JavascriptAbstractClasses.XIS_WIDGET);
    }

    @Test
    @DisplayName("XISPage")
    void page() {
        checkClass(JavascriptAbstractClasses.XIS_PAGE);
    }

    @Test
    @DisplayName("XISConteiner")
    void container() {
        checkClass(JavascriptAbstractClasses.XIS_CONTAINER);
    }

    @Test
    @DisplayName("XISElement")
    void element() {
        checkClass(JavascriptAbstractClasses.XIS_ELEMENT);
    }

    @Test
    @DisplayName("XISHead")
    void headElement() {
        checkClass(JavascriptAbstractClasses.XIS_HEAD);
    }

    @Test
    @DisplayName("XISBody")
    void bodyElement() {
        checkClass(JavascriptAbstractClasses.XIS_BODY);
    }

    @Test
    @DisplayName("XISValueHolder")
    void valueHolder() {
        checkClass(JavascriptAbstractClasses.XIS_VALUE_HOLDER);
    }

    @Test
    @DisplayName("XISStaticTextNode")
    void staticTextNode() {
        checkClass(JavascriptAbstractClasses.XIS_STATIC_TEXT_NODE);
    }

    @Test
    @DisplayName("XISMutableTextNode")
    void mutableTextNode() {
        checkClass(JavascriptAbstractClasses.XIS_MUTABLE_TEXT_NODE);
    }

    @Test
    @DisplayName("XISIf")
    void ifOperator() {
        // TODO checkClass(JavascriptAbstractClasses.XIS_IF);
    }

    @Test
    @DisplayName("XISLoop")
    void loop() {
        // TODO checkClass(JavascriptAbstractClasses.XIS_LOOP);
    }


    private void checkClass(JSSuperClass superClass) {
        checkClass(superClass, IOUtils.getResourceAsString("api/component/" + superClass.getClassName() + ".js"));
    }

    private void checkClass(JSSuperClass superClass, String script) {
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

    private void checkClassDeclaration(JSSuperClass superClass, String script) {
        if (!classDeclarationFound(superClass, script)) {
            throw new AssertionFailedError("class declaration not found: " + superClass.getClassName());
        }
    }

    private boolean classDeclarationFound(JSSuperClass superClass, String script) {
        return Pattern.compile(".*class\\s" + superClass.getClassName() + ".*").matcher(script).find();
    }

    private void checkDeclaredMethods(JSSuperClass superClass, String script) {
        superClass.getDeclaredMethods().values()
                .forEach(method -> checkDeclaredMethod(method.getName(), method.getArgs().size(), script));
    }

    private void checkAbstractMethods(JSSuperClass superClass, String script) {
        superClass.getDeclaredAbstractMethods().values()
                .forEach(method -> checkAbstractMethod(method.getName(), method.getArgs().size(), script));
    }


    private void checkDeclaredMethod(String methodName, int args, String script) {
        if (!containsDeclaredMethodWithoutArgs(methodName, args, script)) {
            throw new AssertionFailedError("method not found: " + methodName);
        }
        if (!containsDeclaredMethodWithArgs(methodName, args, script)) {
            throw new AssertionFailedError("wrong number of args: " + methodName);
        }
    }

    private void checkAbstractMethod(String methodName, int args, String script) {
        if (!containsAbstractMethodWithoutArgs(methodName, args, script)) {
            throw new AssertionFailedError("abstract method not found: " + methodName);
        }
        if (!containsAbstractMethodWithArgs(methodName, args, script)) {
            throw new AssertionFailedError("wrong number of args wrong: " + methodName);
        }
    }

    private boolean containsDeclaredMethodWithoutArgs(String methodName, int args, String script) {
        return patternForMethodWithArgs(methodName, args).matcher(script).find();
    }

    private boolean containsAbstractMethodWithoutArgs(String methodName, int args, String script) {
        return patternForAbstractMethodWithArgs(methodName, args).matcher(script).find();
    }

    private boolean containsDeclaredMethodWithArgs(String methodName, int args, String script) {
        return patternForMethodWithArgs(methodName, args).matcher(script).find();
    }

    private boolean containsAbstractMethodWithArgs(String methodName, int args, String script) {
        return patternForAbstractMethodWithArgs(methodName, args).matcher(script).find();
    }

    private Pattern patternForMethodWithArgs(String methodName, int args) {
        return Pattern.compile(".*" + methodName + "\\(\\s*" + argPattern(args) + "\\s*\\)\\s*\\{.*", Pattern.MULTILINE);
    }


    private Pattern patternForAbstractMethodWithArgs(String methodName, int args) {
        return Pattern.compile(".*" + methodName + "\\(\\s*" + argPattern(args) + "\\s*\\)\\s*\\{(\\s+throw\\s+new\\s+Error\\(['\"]abstract.*)", Pattern.MULTILINE);
    }


    private Pattern patternForMethodWithoutArgs(String methodName, int args) {
        return Pattern.compile(".*" + methodName + "\\([^\\)]*\\)\\s*\\{.*", Pattern.MULTILINE);
    }


    private Pattern patternForAbstractMethodWithoutArgs(String methodName, int args) {
        return Pattern.compile(".*" + methodName + "\\([^\\)]*\\)\\s*\\{(\\s+throw\\s+new\\s+Error\\(['\"]abstract.*)", Pattern.MULTILINE);
    }


    private String argPattern(int numOfArgs) {
        return IntStream.iterate(0, i -> i < numOfArgs, i -> i + 1)
                .mapToObj(i -> PARAM_PATTERN)
                .collect(Collectors.joining("\\s*,\\s*"));
    }


}



