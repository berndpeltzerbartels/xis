package one.xis;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import one.xis.context.XISComponent;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Marks a class as a reusable UI widget component that can be embedded into pages
 * or other widgets in the XIS framework.
 *
 * <p>Widgets are typically referenced by a unique identifier. This identifier is used
 * in the corresponding HTML templates to bind the widget instance to its Java logic.</p>
 *
 * <p>If no explicit ID is provided via {@code @Widget("...")}, the {@code SimpleName}
 * of the Java class is used as the default identifier. This means that two widget classes
 * with the same simple name in different packages cannot be used in the same project unless
 * at least one of them specifies a unique ID explicitly.</p>
 *
 * <p>This annotation also enables integration with dependency injection frameworks.
 * It is compatible with Micronaut and includes {@code @Singleton} and {@code @Qualifier}
 * for proper registration as a bean.</p>
 *
 * <p>This annotation is itself marked with {@link XISComponent}, so classes annotated
 * with {@code @Widget} are automatically discovered and registered for dependency injection.
 * There is no need to annotate the class separately with {@code @XISComponent}.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Widget("userCard")
 * public class UserCardWidget {
 *     @ModelData
 *     public User getUser() {
 *         return currentUser;
 *     }
 * }
 * }</pre>
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Qualifier // for micronaut
@Singleton // for micronaut
@Component
@XISComponent
public @interface Widget {
    String value() default "";
}
