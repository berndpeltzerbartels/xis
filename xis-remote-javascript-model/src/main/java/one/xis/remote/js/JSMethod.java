package one.xis.remote.js;

import lombok.Data;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class JSMethod implements JSElement {
    private final String name;
    private final List<JSParameter> parameters;
    private final List<JSStatement> statements = new ArrayList<>();
    private JSValue returnValue;

    public JSMethod(String name) {
        this.name = name;
        this.parameters = Collections.emptyList();
    }

    public JSMethod(String name, List<JSParameter> parameters) {
        this.name = name;
        this.parameters = parameters;
    }


    @Override
    public void writeJS(PrintWriter writer) {
        writer.append(name);
        writer.append(":");
        writer.append("function(");
        writer.append(parameters.stream().map(JSValue::getName).collect(Collectors.joining(",")));
        writer.append(")");
        writer.append("{");
        statements.forEach(statements -> {
            statements.writeJS(writer);
            writer.append(";");
        });
        if (returnValue != null) {
            writer.write("return ");
            returnValue.writeReferenceJS(writer);
            writer.write(";");
        }
        writer.append("}");
    }

    public void addStatement(JSStatement statement) {
        statements.add(statement);
    }
}
