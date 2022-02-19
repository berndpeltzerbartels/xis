package one.xis.js;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class JSMethod implements JSContext {
    JSClass owner;
    String name;
    List<JSStatement> statements = new ArrayList<>();

    public void addStatement(JSStatement statement) {
        statements.add(statement);
    }

}
