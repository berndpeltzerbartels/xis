package one.xis.template;

import lombok.Data;

@Data
public class ExpressionConstant implements ExpressionArg {
    private final String content;
}
