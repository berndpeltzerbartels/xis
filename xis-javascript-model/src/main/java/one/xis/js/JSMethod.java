package one.xis.js;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Value
public class JSMethod implements JSContext {
    JSClass owner;
    String name;
    int args;
    List<JSStatement> statements = new ArrayList<>();

    // TODO validate number of args
    public void addStatement(JSStatement statement) {
        statements.add(statement);
    }

    @Override
    public String toString() {
        return owner.getClassName() + "#" + name + "(...)";
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(owner.getClassName(), name);
    }
}
