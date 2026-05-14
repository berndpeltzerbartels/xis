package one.xis;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import one.xis.context.Component;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Marks a controller as a route-only controller.
 *
 * <p>A router has no template. It matches incoming page URLs and returns a
 * navigation response from a {@link Route} method.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@Singleton
@org.springframework.stereotype.Component
@Component
@Documented
public @interface Router {
    @AliasFor(annotation = org.springframework.stereotype.Component.class)
    String value();
}
