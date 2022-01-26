package one.xis.remote.javascript;

import lombok.Data;

@Data
public class JSAssignment implements JSStatement {
    private final JSAssignable assignable;
    private final String expression;
}
