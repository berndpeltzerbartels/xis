package one.xis.deserialize;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import one.xis.UserContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

@Data
@RequiredArgsConstructor
public final class DeserializationContext {
    private final String path;
    private final AnnotatedElement target;
    private final Class<? extends Annotation> annotationClass;
    private final UserContext userContext;

}
