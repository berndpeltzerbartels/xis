package one.xis;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import one.xis.context.Component;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Marks a class as a modal dialog controller.
 *
 * <p>A modal uses the same controller method annotations as pages and frontlets:
 * {@link ModelData}, {@link FormData}, {@link Action}, validation annotations,
 * and storage annotations. The template is resolved by the same class-name
 * convention as pages and frontlets, or with {@link HtmlFile}.</p>
 *
 * <p>The annotation value is an optional public modal path, for example
 * {@code @Modal("/customers/edit")}. HTML templates usually open a modal by id
 * with {@code xis:modal="EditCustomerModal"} and pass dynamic values with
 * {@code xis:parameter}. Java actions can also open a modal with
 * {@link ModalResponse#open(Class)} or by a concrete modal path.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Qualifier
@Singleton
@org.springframework.stereotype.Component
@Component
public @interface Modal {
    @AliasFor(annotation = org.springframework.stereotype.Component.class)
    String value() default "";

    String id() default "";

    String title() default "";
}
