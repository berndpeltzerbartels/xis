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
        doRecursiveProcess(path, target, value, userContext, reportedErrors);
        if (value == null) {
            return;
        }
        doRecursiveProcessClass(path, target, value, userContext, reportedErrors);

    }

    private void doRecursiveProcess(String path, AnnotatedElement target, Object value, UserContext userContext, Collection<ReportedError> reportedErrors) {
        for (var annotation : target.getAnnotations()) {
            if (isJavaAnnotation(annotation)) {
                continue;
            }
            var context = new ReportedErrorContext(path, target, annotation.annotationType(), userContext);
            recursiveProcess(context, annotation.annotationType(), value, reportedErrors);
        }
    }

    private void doRecursiveProcessClass(String path, AnnotatedElement target, Object value, UserContext userContext, Collection<ReportedError> reportedErrors) {
        for (var annotation : value.getClass().getAnnotations()) {
            if (isJavaAnnotation(annotation)) {
                continue;
            }
            var context = new ReportedErrorContext(path, target, annotation.annotationType(), userContext);
            recursiveProcess(context, annotation.annotationType(), value, reportedErrors);
        }
    }

    private void recursiveProcess(ReportedErrorContext context, AnnotatedElement currentElement, Object value, Collection<ReportedError> reportedErrors) {
        for (var annotation : currentElement.getAnnotations()) {
            if (isJavaAnnotation(annotation)) {
                continue;
            }
            if (annotation.annotationType().equals(PostProcessor.class)) {
                if (isJavaAnnotation(annotation)) {
                    continue;
                }
                var postProcessorClass = ((PostProcessor) annotation).value();
                var postProcessor = getPostProcessor(postProcessorClass);
                postProcessor.postProcess(context, value, reportedErrors);
            }
            recursiveProcess(context, annotation.annotationType(), value, reportedErrors);
        }
    }

    private boolean isJavaAnnotation(Annotation annotation) {
        return annotation.annotationType().getPackageName().startsWith("java") || annotation.annotationType().getPackageName().startsWith("javax");
    }

    private DeserializationPostProcessor getPostProcessor(Class<? extends DeserializationPostProcessor> processorClass) {
        return postProcessors.stream()
                .filter(processorClass::isInstance)
                .findFirst().orElseGet(() -> ClassUtils.newInstance(processorClass));
    }
}
