package one.xis.remote.js;

import lombok.Data;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class JSMethod implements JSElement {
    private final String name;
    private final List<JSParameter> parameters;
    private final List<JSStatement> statements = new ArrayList<>();

    public JSMethod(String name) {
        this.name = name;
        this.parameters = Collections.emptyList();
    }

    public JSMethod(String name, List<JSParameter> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public JSMethod(String name, JSParameter... parameters) {
        this.name = name;
        this.parameters = Arrays.asList(parameters);
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
        writer.append("}");
    }

    public JSStatement addStatement(JSStatement statement) {
        statements.add(statement);
        return statement;
    }
}
