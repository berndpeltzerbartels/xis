package one.xis;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects a named or positional XIS UI parameter into a controller method parameter.
 *
 * <p>{@code @Parameter} is the shared parameter annotation for actions,
 * frontlets, and modals. A value can be supplied by a child
 * {@code <xis:parameter>} tag, by a {@link FrontletResponse}, by a
 * {@link ModalResponse}, or by a query string on a frontlet or modal target such
 * as {@code ProductDetails?productId=42} or
 * {@code /customer/edit?customerId=42}. Frontlet and modal query strings are
 * still read with {@code @Parameter}; {@link QueryParameter} is reserved for the
 * query string of the current page URL.</p>
 *
 * <p>Methods annotated with {@link Action}, {@link ModelData}, {@link FormData},
 * {@link Title}, or storage annotations such as {@link LocalStorage},
 * {@link SessionStorage}, and {@link ClientStorage} may receive
 * {@code @Parameter} arguments when they belong to a page, frontlet, or modal
 * controller. In an action method, a named {@code @Parameter} first reads the
 * submitted action parameter. If no action parameter with that name exists, XIS
 * falls back to the current frontlet or modal parameter. This lets frontlet and
 * modal actions read both values submitted by the clicked element and values
 * that identify the surrounding frontlet or modal instance.</p>
 *
 * <p>For positional action calls, use {@code @Parameter} without a
 * {@link #value() name}. XIS then consumes the next positional action value,
 * independent of other injected method parameters such as {@link SharedValue},
 * {@link ClientId}, {@link UserId}, {@link PathVariable}, or
 * {@link QueryParameter}, and unannotated framework parameters such as
 * {@link UserContext}. If an explicit {@link #index()} is used, the index is
 * 1-based and also counts only action values, not Java method parameters.</p>
 *
 * <p>A parameter of type {@code Map<String, String>} with an unnamed
 * {@code @Parameter} receives all current frontlet or modal parameters.</p>
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {
    String value() default "";

    /**
     * Explicit 1-based positional action argument index. Leave at {@code -1}
     * to consume the next positional action value.
     */
    int index() default -1;
}
