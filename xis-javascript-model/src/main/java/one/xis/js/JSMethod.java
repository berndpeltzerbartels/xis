package one.xis.js;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@EqualsAndHashCode(callSuper = false)
public class JSMethod implements JSContext {
    JSClass owner;
    String name;
    List<String> args;
    List<JSStatement> statements = new ArrayList<>();

    // TODO validate number of args
    public void addStatement(JSStatement statement) {
        statements.add(statement);
    }

    @Override
    public String toString() {
        return owner.getClassName() + "#" + name + "(...)";
    }

}
