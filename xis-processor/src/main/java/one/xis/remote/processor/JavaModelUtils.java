package one.xis.remote.processor;

import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.ClassUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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

    Optional<TypeElement> getAnnotation(String qualifiedName, Element e) {
        return getAnnotationMirror(qualifiedName, e)
                .map(AnnotationMirror::getAnnotationType)
                .map(DeclaredType::asElement)
                .map(TypeElement.class::cast);
    }

    Stream<AnnotationMirror> getAnnotationMirrors(String qualifiedName, Element e) {
        return e.getAnnotationMirrors().stream()
                .filter(mirror -> getQualifiedName(mirror).equals(qualifiedName))
                .map(AnnotationMirror.class::cast);
    }

    Optional<AnnotationMirror> getAnnotationMirror(String qualifiedName, Element e) {
        return getAnnotationMirrors(qualifiedName, e).findFirst();
    }

    private String getQualifiedName(AnnotationMirror mirror) {
        return binaryName(ClassUtils.cast(mirror.getAnnotationType().asElement(), TypeElement.class));
    }

    Object getAnnotationValue(AnnotationMirror mirror, String key) {
        return getAnnotationValues(mirror).get(key);
    }

    Map<String, Object> getAnnotationValues(AnnotationMirror mirror) {
        return mirror.getElementValues().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getSimpleName().toString(), e -> e.getValue().getValue()));
    }

    String getPackageName(TypeElement typeElement) {
        return ((PackageElement) typeElement.getEnclosingElement()).getQualifiedName().toString();
    }

    String getSimpleName(TypeElement typeElement) {
        return typeElement.getSimpleName().toString();
    }


    Set<String> getFieldNames(TypeElement typeElement) {
        return typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .filter(VariableElement.class::isInstance)
                .map(VariableElement.class::cast)
                .map(VariableElement::getSimpleName)
                .map(Name::toString)
                .collect(Collectors.toSet());
    }
}
