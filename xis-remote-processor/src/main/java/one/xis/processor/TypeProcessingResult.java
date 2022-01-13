package one.xis.processor;

import lombok.RequiredArgsConstructor;

import javax.lang.model.element.TypeElement;

@RequiredArgsConstructor
class TypeProcessingResult {
    private final TypeElement type;

    void addTypeAnnotationHandler(CodeWriter codeWriter) {

    }

    void addFieldAnnotationHandler(CodeWriter codeWriter) {

    }

    void addMethodAnnotationHandler(CodeWriter codeWriter) {

    }
}
