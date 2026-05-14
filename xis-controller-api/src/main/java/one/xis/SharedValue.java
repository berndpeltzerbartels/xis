package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Provides or injects a named value inside one controller processing flow.
 *
 * <p>Use this to avoid duplicating expensive or central lookup logic inside a
 * controller. A method annotated with {@code @SharedValue("product")} returns
 * a value that can be injected into later model, form-data, action, title, or
 * other shared-value methods with a parameter annotated
 * {@code @SharedValue("product")}.</p>
 *
 * <p>The value is scoped to the current request/action processing flow. It is
 * not persisted across requests and is not browser-side storage. Use
 * {@link LocalStorage}, {@link SessionStorage}, or {@link ClientStorage} for
 * explicit client-side state.</p>
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SharedValue {
    String value();
}
