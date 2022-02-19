package one.xis.template;

import lombok.Data;

/**
 * Number or string variable inside expression.
 * <p>
 * Examples :
 * <pre><code>
 *  ${myFunction(100)} => 100 is an {@link ExpressionContent}
 *  ${myFunction(SOME_CONSTANT)} => SOME_CONSTANT is an {@link ExpressionContent}
 * </code>
 * </pre>
 *
 * <p>
 */
@Data
public class ExpressionConstant implements ExpressionArg {
    private final String content;
}
