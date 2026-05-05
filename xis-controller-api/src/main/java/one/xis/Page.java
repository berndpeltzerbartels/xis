package one.xis;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import one.xis.context.Component;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Marks a class as a XIS page controller and maps it to a browser URL.
 *
 * <p>The annotation value is the public page path, for example
 * {@code @Page("/products/{id}.html")}. Path variables declared in the value
 * can be injected into model and action methods with {@link PathVariable}.</p>
 *
 * <p>The page template is usually resolved by convention from the controller
 * class name and package. Use {@link HtmlFile} when the template uses a
 * different file name.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@Singleton
@org.springframework.stereotype.Component // for spring
@Component
@Documented
public @interface Page {
    @AliasFor(annotation = org.springframework.stereotype.Component.class)
    String value();
}
