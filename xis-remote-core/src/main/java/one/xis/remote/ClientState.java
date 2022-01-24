package one.xis.remote;

import java.lang.annotation.*;

/**
 * An onnotated class will be used as client-state. Parameters of this class
 * do not need to be annotated, because it can be identified as state.
 * <p>
 * If you are using a map as state, a parameter of this state must be annotated as state at least,
 * to find its name.
 */
@Documented
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface ClientState {
    String value() default "";
}
