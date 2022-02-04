package one.xis.remote.js;

import lombok.Data;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Data
public class JSObjectInstance implements JSValue {
    private final String name;
    private final Collection<JSField> fields = new ArrayList<>();
    private final Collection<JSMethodDeclaration> methods = new ArrayList<>();

    @Override
    public void writeJS(PrintWriter writer) {
        writer.append("var ");
        writer.append(name);
        writer.append("={");
        var fieldIterator = fields.iterator();

        while (fieldIterator.hasNext()) {
            fieldIterator.next().writeJS(writer);
            if (fieldIterator.hasNext()) {
                writer.append(",");
            }
        }

        if (!methods.isEmpty()) {
            writer.append(",");
        }

        var methodIterator = methods.iterator();
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


    public JSMethodDeclaration addMethod(String name, String... parameterNames) {
        return addMethod(new JSMethodDeclaration(name, Arrays.stream(parameterNames).map(JSParameter::new).collect(Collectors.toList())));
    }

    public JSMethodDeclaration addMethod(JSMethodDeclaration method) {
        methods.add(method);
        return method;
    }

    @Override
    public void writeReferenceJS(PrintWriter writer) {
        writer.write(name);
    }
}
