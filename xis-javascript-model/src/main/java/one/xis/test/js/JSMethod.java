package one.xis.test.js;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class JSMethod implements JSContext {
    JSClass declaringClass;
    String name;
    List<String> args;
    List<JSStatement> statements = new ArrayList<>();

    // TODO validate number of args
    public void addStatement(JSStatement statement) {
        statements.add(statement);
    }

    @Override
    public String toString() {
        return declaringClass.getClassName() + "#" + name + "(...)";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JSMethod method = (JSMethod) o;
        if (method.args != args) {
            return false;
        }
        if (!method.name.equals(name)) {
            return false;
        }
        if (!method.declaringClass.equals(declaringClass)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return (name + "/" + args).hashCode();
    }
}
