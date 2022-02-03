package one.xis.remote.js;

import lombok.Data;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Data
public class JSIf implements JSBlockStatement {
    private final List<JSStatement> statements = new ArrayList<>();
    private final JSValue value;

    @Override
    public void writeJS(PrintWriter writer) {
        writer.append("if (");
        writer.append(value.getName());
        writer.append("){");
        statements.forEach(statement -> statement.writeJS(writer));
        writer.append("}");
    }

    public void addStatement(JSStatement statement) {
        statements.add(statement);
    }
}
