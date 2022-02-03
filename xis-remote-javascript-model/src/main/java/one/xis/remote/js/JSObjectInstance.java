package one.xis.remote.js;

import lombok.Data;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

@Data
public class JSObjectInstance implements JSValue {
    private final String name;
    private final Collection<JSField> fields = new ArrayList<>();
    private final Collection<JSMethod> methods = new ArrayList<>();

    @Override
    public void writeJS(PrintWriter writer) {
        writer.append("var ");
        writer.append(name);
        writer.append("={");
        Iterator<JSField> fieldIterator = fields.iterator();

        while (fieldIterator.hasNext()) {
            fieldIterator.next().writeJS(writer);
            if (fieldIterator.hasNext()) {
                writer.append(",");
            }
        }

        if (!methods.isEmpty()) {
            writer.append(",");
        }

        Iterator<JSMethod> methodIterator = methods.iterator();
        while (methodIterator.hasNext()) {
            methodIterator.next().writeJS(writer);
            if (methodIterator.hasNext()) {
                writer.append(",");
            }
        }

        writer.append("};");

    }

    public JSField addField(String name, String defaultValue) {
        return addField(new JSField(name, defaultValue));
    }

    public JSField addStringField(String name, String defaultValue) {
        return addField(new JSField(name, "'" + defaultValue + "'"));
    }

    public JSField addField(JSField field) {
        fields.add(field);
        return field;
    }


    public JSMethod addMethod(String name, String... parameterNames) {
        return addMethod(new JSMethod(name, Arrays.stream(parameterNames).map(JSParameter::new).collect(Collectors.toList())));
    }

    public JSMethod addMethod(JSMethod method) {
        methods.add(method);
        return method;
    }

    @Override
    public void writeReferenceJS(PrintWriter writer) {
        writer.write(name);
    }
}
