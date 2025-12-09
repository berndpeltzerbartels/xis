package one.xis;

import java.lang.annotation.*;

/**
 * Exactly one of the pages of the app has to be annotated with
 * this one. That page will be displayed as a default if no other
 * url-mapping matches.
 * 
 * If the page has path variables, specify a concrete URL.
 * Example: @WelcomePage("/products/electronics.html")
 * The URL must match the page's URL pattern.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WelcomePage {
    /**
     * Concrete URL to use for the welcome page.
     * Required if the page has path variables.
     * Must match the page's URL pattern.
     * @return the concrete URL, or empty string if page has no path variables
     */
    String value() default "";
}
