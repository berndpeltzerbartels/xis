package one.xis.context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Runtime context for a method invocation intercepted by an {@link Advice}.
 */
public interface AdviceInvocation {

    AppContext appContext();

    Object target();

    Method method();

    Object[] args();

    List<Annotation> annotations();

    Object proceed() throws Throwable;

    default <A extends Annotation> Optional<A> annotation(Class<A> annotationType) {
        return annotations().stream()
                .filter(annotationType::isInstance)
                .map(annotationType::cast)
                .findFirst();
    }
}
