package one.xis;

import java.lang.annotation.*;

/**
 * Marks the page or router route that should be shown when no more specific page URL matches.
 *
 * <p>An application should have at most one welcome page. The annotation can be used on a {@link Page} controller,
 * on a {@link Route} method, or on a {@link Router} controller that declares exactly one route.</p>
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface WelcomePage {
}
