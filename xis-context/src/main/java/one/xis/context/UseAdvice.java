package one.xis.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an annotation as an AOP advice annotation.
 * <p>
 * XIS applies advice only through JDK interface proxies. A component or bean that needs advice must therefore be used
 * through one of its interfaces.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseAdvice {

    /**
     * Advice class that should handle method invocations annotated with the annotated annotation.
     */
    Class<? extends Advice> value();
}
