package one.xis.js;

import lombok.Data;

@Data
public class JSCustomStatement implements JSStatement {
    private final String code;
}
