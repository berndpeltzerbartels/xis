package one.xis.template;

import lombok.Value;

/**
 * String inside expression or attribute with variables
 * <p>
 * Examples :
 * <pre><code>
 *  ${oddOrEven(index, 'class1','class2')} => class1 and class2 will be represented by an ExpressionStrings
 *  &lta href="/products/${product.id}"> => /products will be represented by an ExpressionStrings
 * </code>
 * </pre>
 *
 * <p>
 */
@Value
public class ExpressionString implements ExpressionArg {
    String content;
}
