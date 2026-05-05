package one.xis;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import one.xis.context.Component;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Marks a class as a reusable UI frontlet component that can be embedded into pages
 * or other frontlets in the XIS framework.
 *
 * <p>Frontlets are typically referenced by a unique identifier. This identifier is used
 * in the corresponding HTML templates to bind the frontlet instance to its Java logic.</p>
 *
 * <p>If no explicit ID is provided via {@code @Frontlet("...")}, the {@code SimpleName}
 * of the Java class is used as the default identifier. This means that two frontlet classes
 * with the same simple name in different packages cannot be used in the same project unless
 * at least one of them specifies a unique ID explicitly.</p>
 *
 * <p>This annotation also enables integration with the dependency injection
 * runtime used by the selected XIS integration.</p>
 *
 * <p>This annotation is itself marked with {@link Component}, so classes annotated
 * with {@code @Frontlet} are automatically discovered and registered for dependency injection.
 * There is no need to annotate the class separately with {@code @Component}.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Qualifier
@Singleton
@org.springframework.stereotype.Component
@Component
public @interface Frontlet {
    @AliasFor(annotation = org.springframework.stereotype.Component.class)
    String value() default "";

    String id() default "";

    String url() default "";

    String title() default "";

    String containerId() default "";
}
