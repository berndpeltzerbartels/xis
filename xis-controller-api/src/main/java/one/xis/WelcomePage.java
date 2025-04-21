package one.xis;

import java.lang.annotation.*;

/**
 * Exactly one of the pages of the app has to be annotated with
 * this one. That page will be displayed as a default if no other
 * url-mapping matches.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WelcomePage {
}
