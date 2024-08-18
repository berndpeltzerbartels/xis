package one.xis.deserialize;

import lombok.RequiredArgsConstructor;
import one.xis.UserContext;
import one.xis.context.XISComponent;
import one.xis.utils.lang.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

@XISComponent
@RequiredArgsConstructor
class PostProcessing {

    private final List<DeserializationPostProcessor> postProcessors;

    void postProcess(String path, Object value, AnnotatedElement target, UserContext userContext, PostProcessingResults results) {
        doRecursiveProcess(path, target, value, userContext, results);
        if (value == null) {
            return;
        }
        doRecursiveProcessClass(path, target, value, userContext, results);

    }

    private void doRecursiveProcess(String path, AnnotatedElement target, Object value, UserContext userContext, PostProcessingResults results) {
        for (var annotation : target.getAnnotations()) {
            if (isJavaAnnotation(annotation)) {
                continue;
            }
            var context = new DeserializationContext(path, target, annotation.annotationType(), userContext);
            recursiveProcess(context, annotation.annotationType(), value, results);
        }
    }

    private void doRecursiveProcessClass(String path, AnnotatedElement target, Object value, UserContext userContext, PostProcessingResults results) {
        for (var annotation : value.getClass().getAnnotations()) {
            if (isJavaAnnotation(annotation)) {
                continue;
            }
            var context = new DeserializationContext(path, target, annotation.annotationType(), userContext);
            recursiveProcess(context, annotation.annotationType(), value, results);
        }
    }

    private void recursiveProcess(DeserializationContext context, AnnotatedElement currentElement, Object value, PostProcessingResults results) {
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
                postProcessor.postProcess(context, value, results);
            }
            recursiveProcess(context, annotation.annotationType(), value, results);
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
