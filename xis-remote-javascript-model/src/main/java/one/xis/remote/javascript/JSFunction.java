package one.xis.remote.javascript;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class JSFunction implements JSElement {
    private final String name;
    private final List<String> parameters;
    private final List<JSStatement> statements = new ArrayList<>();
    protected JSVar returnVar;

    public <S extends JSStatement> S addStatement(S statement) {
        statements.add(statement);
        return statement;
    }

    public void setReturnVar(JSVar varName) {
        this.returnVar = varName;
    }
}
