package one.xis.remote.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import java.io.File;

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
        return new WidgetContext(packageName, className, htmlFile);
    }

}
