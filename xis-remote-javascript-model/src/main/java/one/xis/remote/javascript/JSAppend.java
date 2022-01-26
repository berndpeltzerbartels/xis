package one.xis.remote.javascript;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JSAppend implements JSStatement {
    private final JSAssignable assignable;
    private final String expression;
}
