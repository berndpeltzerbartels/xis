package one.xis.remote.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import java.io.File;

class TemplateContextFactory {
    private final JavaModelUtils javaModelUtils;
    private final ProcessorUtils processorUtils;

    TemplateContextFactory(ProcessingEnvironment environment) {
        javaModelUtils = new JavaModelUtils(environment);
        processorUtils = new ProcessorUtils(environment);
    }

    TemplateContext templateContext(TypeElement element) {
        String packageName = javaModelUtils.getPackageName((TypeElement) element);
        File htmlFile = processorUtils.getFileInSourceFolder(packageName, element.getSimpleName() + ".html");
        return new TemplateContext(packageName, htmlFile);
    }

}
