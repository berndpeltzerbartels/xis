package one.xis.context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Runtime context for a method invocation intercepted by an {@link Advice}.
 */
public interface AdviceInvocation {

    /**
     * @return application context that owns the proxied component
     */
    AppContext appContext();

    /**
     * @return target component behind the proxy
     */
    Object target();

    /**
     * @return invoked interface method
     */
    Method method();

    /**
     * @return arguments passed to the method
     */
    Object[] args();

    /**
     * @return annotations that caused advice to be applied
     */
    List<Annotation> annotations();

    /**
     * Continues invocation by calling the next advice or the target method.
     */
    Object proceed() throws Throwable;

    /**
     * Finds one of the advice annotations on this invocation.
     */
    default <A extends Annotation> Optional<A> annotation(Class<A> annotationType) {
        return annotations().stream()
                .filter(annotationType::isInstance)
                .map(annotationType::cast)
                .findFirst();
    }
}
