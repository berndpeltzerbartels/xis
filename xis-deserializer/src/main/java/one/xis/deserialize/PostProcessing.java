package one.xis.deserialize;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.UserContext;
import one.xis.context.Component;
import one.xis.utils.lang.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PostProcessing {

    private final List<DeserializationPostProcessor> postProcessors;

    void postProcess(@NonNull String path,
                     @NonNull Object value,
                     @NonNull AnnotatedElement target,
                     @NonNull UserContext userContext,
                     @NonNull PostProcessingResults results) {
        doRecursiveProcess(path, target, value, userContext, results);
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

    private void recursiveProcess(@NonNull DeserializationContext context, @NonNull AnnotatedElement currentElement, @NonNull Object value, @NonNull PostProcessingResults results) {
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
