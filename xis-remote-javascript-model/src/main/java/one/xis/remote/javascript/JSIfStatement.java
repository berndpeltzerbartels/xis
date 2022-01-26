package one.xis.remote.javascript;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class JSIfStatement implements JSBlockStatement {
    private final String condition;

    private final List<JSStatement> statements = new ArrayList<>();

    @Override
    public void addStatement(JSStatement statement) {
        statements.add(statement);
    }
    
}
