package one.xis;

import java.lang.annotation.*;

// TODO Kommentar und Umsetzung sind falsch. QueryParameter ist fuer Query-Parameter, nicht fuer Path-Parameter

/**
 * Annotation  for url-parameters {@link QueryParameter}. url parameters
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
 * To use this url-parameter as a method parameter, it has to be annotated with {@link QueryParameter} like this:
 * <pre>
 *     <code>
 *         public void method(@QueryParameter("a") String a) {
 *         ...
 *         }
 *         </code>
 *  </pre>
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryParameter {
    String value();
}
