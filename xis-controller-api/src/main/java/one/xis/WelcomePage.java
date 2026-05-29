package one.xis;

import java.lang.annotation.*;

/**
 * Marks the page that should be shown when no more specific page URL matches.
 *
 * <p>An application should have at most one welcome page. The annotated class must also be a {@link Page} controller.</p>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WelcomePage {
}
