package one.xis.remote.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Set;
import java.util.stream.Collectors;

public class MainProcessor extends AnnotationProcessor {

    private ProcessingResult processingResult;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return AnnotationHandlerRegistry.getTypeAnnotations()
                .map(javaModelUtils::binaryName)
                .collect(Collectors.toSet());
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        processingResult = new ProcessingResult(javaModelUtils);
    }

    @Override
    void doProcess(Element element, TypeElement annotationType, RoundEnvironment roundEnv) throws ValidationException {
        doProcessType((TypeElement) element, annotationType, roundEnv);
    }

    private void doProcessType(TypeElement type, TypeElement annotationType, RoundEnvironment roundEnv) {
        AnnotationHandlerRegistry.getHandlerForTypeAnnotation(annotationType)
                .forEach(handler -> {
                    doProcessType(type, handler, roundEnv);
                    doProcessFields(type, roundEnv);
                    doProcessMethod(type, roundEnv);
                });
    }

    private void doProcessType(TypeElement type, CodeWriter codeWriter, RoundEnvironment roundEnv) {

    }

    private void doProcessFields(TypeElement type, RoundEnvironment roundEnv) {
        type.getEnclosedElements().stream()
                .filter(javaModelUtils::isField)
                .filter(VariableElement.class::isInstance) // redundant to avoid warning
                .map(VariableElement.class::cast)
                .forEach(field -> doProcessField(field, roundEnv));
    }

    private void doProcessField(VariableElement field, RoundEnvironment roundEnv) {
        javaModelUtils.getAnnotations(field).flatMap(AnnotationHandlerRegistry::getHandlerForFieldAnnotation)
                .forEach(handler -> doProcessField(field, handler, roundEnv));
    }

    private void doProcessField(VariableElement field, CodeWriter codeWriter, RoundEnvironment roundEnv) {

    }

    private void doProcessMethod(TypeElement type, RoundEnvironment roundEnv) {

    }


    @Override
    void finish() {

    }
}
