package one.xis.deserialize;

import lombok.RequiredArgsConstructor;
import one.xis.UserContext;
import one.xis.context.XISComponent;
import one.xis.utils.lang.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.List;

@XISComponent
@RequiredArgsConstructor
class DeserializationPostProcessing {

    private final List<DeserializationPostProcessor> postProcessors;

    void postProcess(String path, Object value, AnnotatedElement target, UserContext userContext, Collection<ReportedError> reportedErrors) {
        for (var annotation : target.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(PostProcessor.class)) {
                doPostProcess(annotation, path, value, target, userContext, reportedErrors);
            }
        }
        if (value == null) {
            return;
        }
        for (var annotation : value.getClass().getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(PostProcessor.class)) {
                doPostProcess(annotation, path, value, target, userContext, reportedErrors);
            }
        }
    }

    private void doPostProcess(Annotation annotation, String path, Object value, AnnotatedElement target, UserContext userContext, Collection<ReportedError> reportedErrors) {
        var postProcessorClass = annotation.annotationType().getAnnotation(PostProcessor.class).value();
        var postProcessor = getPostProcessor(postProcessorClass);
        var deserializationContext = new ReportedErrorContext(path, target, annotation.annotationType(), userContext);
        postProcessor.postProcess(deserializationContext, value, reportedErrors);
    }

    private DeserializationPostProcessor getPostProcessor(Class<? extends DeserializationPostProcessor> processorClass) {
        return postProcessors.stream()
                .filter(processorClass::isInstance)
                .findFirst().orElseGet(() -> ClassUtils.newInstance(processorClass));
    }
}
