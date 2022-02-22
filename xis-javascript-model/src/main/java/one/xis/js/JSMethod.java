package one.xis.js;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class JSMethod implements JSContext {
    JSClass owner;
    String name;
    String[] args;
    List<JSStatement> statements = new ArrayList<>();

    public JSMethod(JSClass owner, String name, String... args) {
        this.owner = owner;
        this.name = name;
        this.args = args;
    }

    // TODO validate number of args
    public void addStatement(JSStatement statement) {
        statements.add(statement);
    }

}
