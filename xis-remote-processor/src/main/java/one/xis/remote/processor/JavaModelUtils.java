package one.xis.remote.processor;

import lombok.RequiredArgsConstructor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.stream.Stream;

@RequiredArgsConstructor
class JavaModelUtils {
    private final ProcessingEnvironment environment;

    String binaryName(TypeElement type) {
        return getName(type).toString();
    }

    Name getName(TypeElement type) {
        return environment.getElementUtils().getBinaryName(type);
    }

    boolean isField(Element e) {
        return e.getKind() == ElementKind.FIELD;
    }

    boolean isAnnotation(Element e) {
        return e.getKind() == ElementKind.ANNOTATION_TYPE;
    }

    boolean hasAnnotation(Element element, TypeElement annotation) {
        String annotationName = binaryName(annotation);
        return getAnnotations(element).map(this::binaryName).anyMatch(annotationName::equals);
    }

    Element asElement(TypeMirror typeMirror) {
        return environment.getTypeUtils().asElement(typeMirror);
    }

    Element asElement(AnnotationMirror mirror) {
        return mirror.getAnnotationType().asElement();
    }

    Stream<TypeElement> getAnnotations(Element e) {
        return e.getAnnotationMirrors().stream()
                .map(this::asElement)
                .filter(this::isAnnotation)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast);
    }

    String getPackageName(TypeElement typeElement) {
        return ((PackageElement) typeElement.getEnclosingElement()).getQualifiedName().toString();
    }

    String getSimpleName(TypeElement typeElement) {
        return typeElement.getSimpleName().toString();
    }
}
