package one.xis;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import one.xis.context.Component;

import java.lang.annotation.*;

/**
 * Makes a piece of html loadable for the framework as an include.
 * E.g. if the annotation's value is "navigation", on a template, the include can be used as:
 * <pre><code>
 * &lt;xis:include name="navigation"/&gt;
 * </code></pre>
 * or
 * <pre><code>
 * &lt;div xis-include="navigation"&gt;&lt;/div&gt;
 * </code></pre> <p>
 * Classes annotated with @Include can not be interfaces or abstract classes.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Qualifier // for micronaut
@Singleton // for micronaut
@org.springframework.stereotype.Component // for spring
@Component
@Documented
public @interface Include {
    String value();
}
