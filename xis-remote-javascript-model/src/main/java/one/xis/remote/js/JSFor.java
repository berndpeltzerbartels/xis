package one.xis.remote.js;

import lombok.Data;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Data
public class JSFor implements JSBlockStatement {
    private final List<JSStatement> statements = new ArrayList<>();
    private final JSValue arrayValue;
    private final String itemVarName;
    private final String itemNrVarName;
    private final String itemIndexVarName;

    @Override
    public void writeJS(PrintWriter writer) {
        writer.append("for(var ");
        writer.append(itemIndexVarName);
        writer.append("=0;");
        writer.append(itemIndexVarName);
        writer.append("<");
        arrayValue.writeReferenceJS(writer);
        writer.append(".length;");
        writer.append(itemIndexVarName);
        writer.append("++){");
        statements.forEach(statement -> statement.writeJS(writer));
        writer.append("}");
    }

    public void addStatement(JSStatement statement) {
        statements.add(statement);
    }
}
