package one.xis.remote.javascript;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class JSForStatement implements JSBlockStatement {
    private final String arrayVar;
    private final String itemVar;
    private final String indexVar;

    private final List<JSStatement> statements = new ArrayList<>();

    @Override
    public void addStatement(JSStatement statement) {
        statements.add(statement);
    }
}
