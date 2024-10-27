package one.xis;

import java.lang.annotation.*;

/**
 * Annotation  for url-parameters {@link URLParameter}. url parameters
 * are defined in value of {@link Page} annotation.
 * <p>
 * Example:
 * <pre>
 *     <code>
 *         @Page("/{a}/xyz.html")
 *         class ExamplePage {
 *         ...
 *     </code>
 * </pre>
 * <p>
 * The name of the parameter in the example is "a".
 * To use this url-parameter as a method parameter, it has to be annotated with {@link URLParameter} like this:
 * <pre>
 *     <code>
 *         public void method(@URLParameter("a") String a) {
 *         ...
 *         }
 *         </code>
 *  </pre>
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface URLParameter {
    String value();
}
