package one.xis;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import one.xis.context.Component;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Marks a class as a reusable UI fragment that can be rendered inside a frontlet
 * container.
 *
 * <p>A frontlet is usually selected explicitly from an HTML template, for example
 * with {@code xis:default-frontlet}, {@code xis:frontlet}, or by returning the
 * frontlet class from an action method. In those cases XIS resolves the target
 * frontlet by its id. If no explicit id is provided, the simple Java class name is
 * used. Both {@link #value()} and {@link #id()} can define that id. If both are
 * set, {@link #id()} wins.</p>
 *
 * <p>{@link #containerId()} can be used when the frontlet normally belongs to one
 * container. It is most useful for frontlet classes returned from action methods or
 * for URL-selected frontlets, because XIS can then determine the target container
 * from the annotation. Template attributes such as {@code xis:target-container}
 * may still override the target for a concrete link or button.</p>
 *
 * <p>{@link #url()} restricts the frontlet to browser URLs that match the given
 * pattern. Combined with {@link #containerId()}, this lets a page define an empty
 * container and lets XIS choose the matching frontlet for the current URL. This is
 * different from passing data to a frontlet instance: a frontlet can read path
 * variables from the current page URL with {@link PathVariable} even when the
 * frontlet itself has no {@code url}. Use {@link FrontletParameter} for values that
 * belong to a concrete frontlet instance, such as a selected row id.</p>
 *
 * <p>If two frontlet classes have the same simple name in different packages, give
 * at least one of them an explicit id.</p>
 *
 * <p>This annotation also enables integration with the dependency injection runtime
 * used by the selected XIS integration. It is itself marked with {@link Component},
 * so classes annotated with {@code @Frontlet} are automatically discovered and
 * registered for dependency injection. There is no need to annotate the class
 * separately with {@code @Component}.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Qualifier
@Singleton
@org.springframework.stereotype.Component
@Component
@ImportInstances
public @interface Frontlet {
    /**
     * Frontlet id shortcut, for example {@code @Frontlet("CustomerList")}. If this
     * and {@link #id()} are both empty, the simple Java class name is used.
     */
    @AliasFor(annotation = org.springframework.stereotype.Component.class)
    String value() default "";

    /**
     * Explicit frontlet id. This is the id used by {@code xis:default-frontlet},
     * {@code xis:frontlet}, and frontlet class responses. It takes precedence over
     * {@link #value()}.
     */
    String id() default "";

    /**
     * Optional browser URL pattern for URL-selected frontlets. The pattern may
     * contain path variables such as {@code /employees/{group}/list.html}. Use this
     * together with {@link #containerId()} when the current page URL should decide
     * which frontlet occupies a container.
     */
    String url() default "";

    /**
     * Optional browser title to apply when this frontlet is loaded as the result of
     * a frontlet navigation.
     */
    String title() default "";

    /**
     * Optional default target container for this frontlet. This is useful when an
     * action returns the frontlet class or when {@link #url()} is used to select a
     * frontlet for the current page URL. A template can still provide a concrete
     * target container for a specific link or button.
     */
    String containerId() default "";
}
