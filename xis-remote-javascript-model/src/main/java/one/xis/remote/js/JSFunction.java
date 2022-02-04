package one.xis.remote.js;

import lombok.Data;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class JSFunction implements JSElement {
    private final String name;
    private final List<JSParameter> parameters;
    private final List<JSStatement> statements = new ArrayList<>();

    public JSFunction(String name, String... parameters) {
        this.name = name;
        this.parameters = Arrays.stream(parameters).map(JSParameter::new).collect(Collectors.toList());
    }

    public JSFunction(String name, List<JSParameter> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    @Override
    public void writeJS(PrintWriter writer) {
        writer.append("function ");
        writer.append(name);
        writer.append("(");
        writer.append(parameters.stream().map(JSValue::getName).collect(Collectors.joining(",")));
        writer.append(")");
        writer.append("{");
        statements.forEach(statements -> {
            statements.writeJS(writer);
            writer.append(";");
        });
        writer.append("}");
    }

    public void addStatement(JSStatement statement) {
        statements.add(statement);
    }
}
