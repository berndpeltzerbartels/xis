package one.xis.template;

import lombok.Data;

@Data
public class ExpressionString implements ExpressionArg {
    private final String content;
}
