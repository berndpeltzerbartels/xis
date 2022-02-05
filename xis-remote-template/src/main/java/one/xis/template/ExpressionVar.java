package one.xis.template;

import lombok.Data;

@Data
public class ExpressionVar implements ExpressionArg {
    private final String varName;
}
