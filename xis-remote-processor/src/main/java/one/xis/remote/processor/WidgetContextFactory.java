package one.xis.remote.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import java.io.File;
import java.util.Optional;

class WidgetContextFactory {
    private final JavaModelUtils javaModelUtils;
    private final ProcessorUtils processorUtils;

    WidgetContextFactory(ProcessingEnvironment environment) {
        javaModelUtils = new JavaModelUtils(environment);
        processorUtils = new ProcessorUtils(environment);
    }

    WidgetContext templateContext(TypeElement element) {
        String packageName = javaModelUtils.getPackageName(element);
        String className = javaModelUtils.getSimpleName(element);
        File htmlFile = processorUtils.getFileInSourceFolder(packageName, element.getSimpleName() + ".html");
        String httpPath = getHttpPath(element).orElse(null);
        return new WidgetContext(packageName, className, htmlFile, httpPath);
    }
    
    private Optional<String> getHttpPath(TypeElement element) {
        return javaModelUtils.getAnnotationMirror("one.xis.remote.Page", element)
                .map(mirror -> javaModelUtils.getAnnotationValue(mirror, "value"))
                .map(String.class::cast);
    }

}
