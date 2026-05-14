package one.xis.processor;

import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Validation processor for XIS applications.
 */
@SupportedOptions({"xis.projectDir", "xis.allErrors"})
@AutoService(Processor.class)
public class XISValidateProcessor extends AbstractProcessor {

    private static final String OPTION_PROJECT_DIR = "xis.projectDir";
    private static final String OPTION_ALL_ERRORS = "xis.allErrors";
    private static final Set<String> CONTROLLER_ANNOTATIONS = Set.of(
            "one.xis.Page",
            "one.xis.Frontlet",
            "one.xis.Modal"
    );
    private static final String HTML_FILE_ANNOTATION = "one.xis.HtmlFile";
    private static final String MODEL_DATA_ANNOTATION = "one.xis.ModelData";
    private static final String FORM_DATA_ANNOTATION = "one.xis.FormData";

    private boolean processed;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of("*");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (processed || roundEnv.processingOver()) {
            return false;
        }
        processed = true;

        Path projectDir = resolveProjectDir();
        if (projectDir == null) {
            return false;
        }

        XisTemplateValidator validator = new XisTemplateValidator(failFast());
        List<ControllerTemplateModel> controllers = collectControllerModels(roundEnv, projectDir);
        List<ValidationError> errors = validator.validate(projectDir, controllers);
        for (ValidationError error : errors) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error.message());
        }

        return false;
    }

    private Path resolveProjectDir() {
        String value = processingEnv.getOptions().get(OPTION_PROJECT_DIR);
        if (value == null || value.isBlank()) {
            return null;
        }
        return Path.of(value);
    }

    private boolean failFast() {
        return !Boolean.parseBoolean(processingEnv.getOptions().get(OPTION_ALL_ERRORS));
    }

    private List<ControllerTemplateModel> collectControllerModels(RoundEnvironment roundEnv, Path projectDir) {
        List<ControllerTemplateModel> controllers = new ArrayList<>();
        for (TypeElement controllerType : findControllerTypes(roundEnv)) {
            controllers.add(controllerModel(projectDir, controllerType));
        }
        return controllers;
    }

    private Set<TypeElement> findControllerTypes(RoundEnvironment roundEnv) {
        Set<TypeElement> controllerTypes = new LinkedHashSet<>();
        for (String annotationName : CONTROLLER_ANNOTATIONS) {
            TypeElement annotationType = processingEnv.getElementUtils().getTypeElement(annotationName);
            if (annotationType == null) {
                continue;
            }
            for (Element element : roundEnv.getElementsAnnotatedWith(annotationType)) {
                if (element.getKind() == ElementKind.CLASS) {
                    controllerTypes.add((TypeElement) element);
                }
            }
        }
        return controllerTypes;
    }

    private ControllerTemplateModel controllerModel(Path projectDir, TypeElement controllerType) {
        Set<String> modelDataNames = new LinkedHashSet<>();
        Set<String> formDataNames = new LinkedHashSet<>();
        for (Element enclosedElement : controllerType.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.METHOD) {
                collectDataMethodNames((ExecutableElement) enclosedElement, modelDataNames, formDataNames);
            }
        }
        Path templateFile = templateFile(projectDir, controllerType);
        return new ControllerTemplateModel(controllerType.getQualifiedName().toString(), templateFile, modelDataNames, formDataNames);
    }

    private void collectDataMethodNames(ExecutableElement method, Set<String> modelDataNames, Set<String> formDataNames) {
        Optional<String> modelDataName = annotationStringValue(method, MODEL_DATA_ANNOTATION, "value");
        Optional<String> formDataName = annotationStringValue(method, FORM_DATA_ANNOTATION, "value");
        modelDataName.map(value -> dataName(method, value)).ifPresent(modelDataNames::add);
        formDataName.map(value -> dataName(method, value)).ifPresent(formDataNames::add);
    }

    private String dataName(ExecutableElement method, String annotationValue) {
        if (annotationValue != null && !annotationValue.isBlank()) {
            return annotationValue;
        }
        String methodName = method.getSimpleName().toString();
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }
        return methodName;
    }

    private Path templateFile(Path projectDir, TypeElement controllerType) {
        Optional<String> htmlFile = annotationStringValue(controllerType, HTML_FILE_ANNOTATION, "value");
        Path sourceRoot = projectDir.resolve("src/main/java");
        if (htmlFile.isPresent()) {
            return htmlFilePath(sourceRoot, controllerType, htmlFile.get());
        }
        return defaultTemplateFile(sourceRoot, controllerType);
    }

    private Path htmlFilePath(Path sourceRoot, TypeElement controllerType, String value) {
        String normalized = value.trim().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (value.trim().startsWith("/") || value.trim().startsWith("\\")) {
            return sourceRoot.resolve(normalized);
        }
        return packagePath(sourceRoot, controllerType).resolve(normalized);
    }

    private Path defaultTemplateFile(Path sourceRoot, TypeElement controllerType) {
        return packagePath(sourceRoot, controllerType).resolve(controllerType.getSimpleName() + ".html");
    }

    private Path packagePath(Path sourceRoot, TypeElement controllerType) {
        String packageName = processingEnv.getElementUtils().getPackageOf(controllerType).getQualifiedName().toString();
        if (packageName.isBlank()) {
            return sourceRoot;
        }
        return sourceRoot.resolve(packageName.replace('.', '/'));
    }

    private Optional<String> annotationStringValue(Element element, String annotationName, String propertyName) {
        for (var mirror : element.getAnnotationMirrors()) {
            var annotationElement = (TypeElement) mirror.getAnnotationType().asElement();
            if (!annotationElement.getQualifiedName().contentEquals(annotationName)) {
                continue;
            }
            for (var method : annotationElement.getEnclosedElements()) {
                if (method.getKind() != ElementKind.METHOD) {
                    continue;
                }
                var executable = (ExecutableElement) method;
                if (!executable.getSimpleName().contentEquals(propertyName)) {
                    continue;
                }
                var explicitValue = mirror.getElementValues().get(executable);
                if (explicitValue != null) {
                    return Optional.ofNullable(explicitValue.getValue()).map(Object::toString);
                }
                var defaultValue = executable.getDefaultValue();
                if (defaultValue != null) {
                    return Optional.ofNullable(defaultValue.getValue()).map(Object::toString);
                }
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
