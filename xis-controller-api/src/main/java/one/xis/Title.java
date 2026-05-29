package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Supplies or injects a browser title for the current page or frontlet lifecycle.
 *
 * <p>On a method, the return value becomes the title sent to the browser. On a parameter, the current title value is
 * injected into another controller method. Title methods may use the usual controller parameters such as
 * {@link PathVariable}, {@link QueryParameter}, {@link FrontletParameter}, or {@link SharedValue}.</p>
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Title {
  
}
