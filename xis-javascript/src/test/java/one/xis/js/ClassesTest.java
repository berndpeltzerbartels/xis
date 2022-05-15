package one.xis.js;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.Test;

import javax.script.CompiledScript;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@SuppressWarnings({"deprecation", "removal"})
class ClassesTest {

    @Test
    void allCompiles() throws ScriptException {
        String functions = IOUtils.getResourceAsString("xis-template-functions.js");
        String classes = IOUtils.getResourceAsString("xis-template-classes.js");

        JSUtil.compile(functions + classes);
    }


    @Test
    @SuppressWarnings("unchecked")
    void xisElement() throws ScriptException {
        String functions = IOUtils.getResourceAsString("xis-template-functions.js");
        String classes = IOUtils.getResourceAsString("xis-template-classes.js");

        CompiledScript script = JSUtil.compile(functions + classes + " new XISElement();");
        Map<String, Object> mirror = (Map<String, Object>) script.eval();

        //TODO Only fields can be checked wit graals-vm
        /*
        Set<String> methodsMirror = methods(null, "element", "children");
        Set<String> methodsJSSuperClass = methods(Classes.XIS_ELEMENT);

        assertThat(methodsMirror).hasSameSizeAs(methodsJSSuperClass);
        assertThat(methodsMirror).containsAll(methodsMirror);
        */
    }


    private Set<String> methods(ScriptObjectMirror mirror, String... fieldNames) {
        Set<String> fieldNameSet = Arrays.stream(fieldNames).collect(Collectors.toSet());
        return mirror.keySet().stream().filter(name -> !fieldNameSet.contains(name)).collect(Collectors.toSet());
    }

    private Set<String> methods(JSSuperClass superClass) {
        return superClass.getAllMethods().values().stream().map(JSMethod::getName).collect(Collectors.toSet());
    }
}