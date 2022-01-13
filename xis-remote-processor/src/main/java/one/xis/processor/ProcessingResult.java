package one.xis.processor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
class ProcessingResult {
    private final JavaModelUtils javaModelUtils;

    @Getter
    private final Map<String, TypeProcessingResult> results = new HashMap<>();

    void addTypeAnnotationHandler(TypeElement typeElement, CodeWriter handler) {
        typeResult(typeElement).addTypeAnnotationHandler(handler);
    }

    void addFieldAnnotationHandler(VariableElement field, CodeWriter handler) {
        typeResult((TypeElement) field.getEnclosingElement()).addFieldAnnotationHandler(handler);
    }

    void addMethodAnnotationHandler(VariableElement field, CodeWriter handler) {
        typeResult((TypeElement) field.getEnclosingElement()).addMethodAnnotationHandler(handler);
    }

    private TypeProcessingResult typeResult(TypeElement type) {
        String name = javaModelUtils.binaryName(type);
        return results.computeIfAbsent(name, bin -> new TypeProcessingResult(type));
    }

}
