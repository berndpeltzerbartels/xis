package one.xis.remote.javascript;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class JSFunction {
    private final String name;
    private final List<JSVar> parameters;
    private final List<JSStatement> statements = new ArrayList<>();

    protected JSAssignable returnValue;

    JSStatement add(JSStatement statement) {
        statements.add(statement);
        return statement;
    }

    void setReturnVar(String varName) {
        this.returnValue = getParameterByName(varName).orElseGet(() -> getStatementByVarName(varName).orElseThrow(() -> new IllegalStateException("invalid return value: " + varName)));
    }

    protected Optional<JSVar> getParameterByName(String name) {
        return parameters.stream().filter(param -> param.getName().equals(name)).findFirst();
    }

    protected Optional<JSVar> getStatementByVarName(String name) {
        return statements.stream().filter(JSVar.class::isInstance)
                .map(JSVar.class::cast)
                .filter(v -> v.getName().equals(name))
                .findFirst();
    }

}
