package one.xis.remote.javascript;

import lombok.Data;

@Data
public class JSVar implements JSStatement, JSAssignable {
    private final String name;

}
