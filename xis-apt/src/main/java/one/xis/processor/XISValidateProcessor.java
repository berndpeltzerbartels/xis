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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
    private static final String ACTION_ANNOTATION = "one.xis.Action";
    private static final String MODEL_DATA_ANNOTATION = "one.xis.ModelData";
    private static final String FORM_DATA_ANNOTATION = "one.xis.FormData";
    private static final String ACTION_PARAMETER_ANNOTATION = "one.xis.ActionParameter";
    private static final String FRONTLET_PARAMETER_ANNOTATION = "one.xis.FrontletParameter";
    private static final String MODAL_PARAMETER_ANNOTATION = "one.xis.ModalParameter";
    private static final String SHARED_VALUE_ANNOTATION = "one.xis.SharedValue";
    private static final int MAX_PROPERTY_DEPTH = 6;

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
        Map<String, TemplateDataModel> modelData = new LinkedHashMap<>();
        Map<String, TemplateDataModel> formData = new LinkedHashMap<>();
        Set<String> sharedValues = new LinkedHashSet<>();
        List<ExecutableElement> methods = new ArrayList<>();
        for (Element enclosedElement : controllerType.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.METHOD) {
                var method = (ExecutableElement) enclosedElement;
                methods.add(method);
                collectDataMethodNames(method, modelData, formData);
                annotationStringValue(method, SHARED_VALUE_ANNOTATION, "value")
                        .filter(value -> !value.isBlank())
                        .ifPresent(sharedValues::add);
            }
        }
        validateSharedValueParameters(methods, sharedValues);
        validateSharedValueMethods(methods);
        validateActionFormDataLoad(methods);
        validateFormDataParameters(methods);
        validateActionParameters(methods);
        Path templateFile = templateFile(projectDir, controllerType);
        return new ControllerTemplateModel(controllerType.getQualifiedName().toString(), templateFile, modelData, formData);
    }

    private void validateSharedValueParameters(List<ExecutableElement> methods, Set<String> sharedValues) {
        for (ExecutableElement method : methods) {
            for (VariableElement parameter : method.getParameters()) {
                annotationStringValue(parameter, SHARED_VALUE_ANNOTATION, "value")
                        .filter(value -> !value.isBlank())
                        .filter(value -> !sharedValues.contains(value))
                        .ifPresent(value -> processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                "@SharedValue parameter \"" + value + "\" has no matching @SharedValue method in this controller.",
                                parameter));
            }
        }
    }

    private void validateSharedValueMethods(List<ExecutableElement> methods) {
        for (ExecutableElement method : methods) {
            if (!hasAnnotation(method, ACTION_ANNOTATION) || !hasAnnotation(method, SHARED_VALUE_ANNOTATION)) {
                continue;
            }
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "@SharedValue methods must not be annotated with @Action. Shared values are called when another method needs them.",
                    method);
        }
    }

    private void validateActionFormDataLoad(List<ExecutableElement> methods) {
        for (ExecutableElement method : methods) {
            if (!hasAnnotation(method, ACTION_ANNOTATION) || !hasAnnotation(method, FORM_DATA_ANNOTATION)) {
                continue;
            }
            var load = annotationStringValue(method, FORM_DATA_ANNOTATION, "load").orElse("ALWAYS");
            if (!"ALWAYS".equals(load)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "@FormData load is only supported on form initialization methods, not on @Action methods.",
                        method);
            }
        }
    }

    private void validateFormDataParameters(List<ExecutableElement> methods) {
        for (ExecutableElement method : methods) {
            if (hasAnnotation(method, ACTION_ANNOTATION)) {
                continue;
            }
            for (VariableElement parameter : method.getParameters()) {
                if (!hasAnnotation(parameter, FORM_DATA_ANNOTATION)) {
                    continue;
                }
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "@FormData parameters are only supported on @Action methods.",
                        parameter);
            }
        }
    }

    private void validateActionParameters(List<ExecutableElement> methods) {
        for (ExecutableElement method : methods) {
            for (VariableElement parameter : method.getParameters()) {
                var parameterAnnotation = parameterAnnotation(parameter);
                if (parameterAnnotation.isEmpty()) {
                    continue;
                }
                Optional<String> actionParameterError = actionParameterAddressingError(parameter, parameterAnnotation.get());
                if (actionParameterError.isPresent()) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, actionParameterError.get(), parameter);
                    continue;
                }
                Optional<String> mapError = parameterMapError(parameter, parameterAnnotation.get());
                if (mapError.isPresent()) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, mapError.get(), parameter);
                    continue;
                }
                if (!isParameterMap(parameter, parameterAnnotation.get()) && !isSimpleParameterType(parameter.asType())) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            simpleParameterError(parameterAnnotation.get()),
                            parameter);
                }
            }
        }
    }

    private Optional<String> parameterAnnotation(VariableElement parameter) {
        if (hasAnnotation(parameter, ACTION_PARAMETER_ANNOTATION)) {
            return Optional.of(ACTION_PARAMETER_ANNOTATION);
        }
        if (hasAnnotation(parameter, FRONTLET_PARAMETER_ANNOTATION)) {
            return Optional.of(FRONTLET_PARAMETER_ANNOTATION);
        }
        if (hasAnnotation(parameter, MODAL_PARAMETER_ANNOTATION)) {
            return Optional.of(MODAL_PARAMETER_ANNOTATION);
        }
        return Optional.empty();
    }

    private Optional<String> actionParameterAddressingError(VariableElement parameter, String annotationName) {
        if (!ACTION_PARAMETER_ANNOTATION.equals(annotationName)) {
            return Optional.empty();
        }
        var value = annotationStringValue(parameter, annotationName, "value").orElse("");
        var index = annotationStringValue(parameter, annotationName, "index")
                .map(Integer::parseInt)
                .orElse(-1);
        if (index == 0) {
            return Optional.of("@ActionParameter index is 1-based; use index=1 for the first action argument.");
        }
        return Optional.empty();
    }

    private boolean isParameterMap(VariableElement parameter, String annotationName) {
        return isUnnamedParameterMapType(parameter) && parameterMapError(parameter, annotationName).isEmpty();
    }

    private Optional<String> parameterMapError(VariableElement parameter, String annotationName) {
        var value = annotationStringValue(parameter, annotationName, "value").orElse("");
        if (!value.isBlank()) {
            return Optional.empty();
        }
        if (!isUnnamedParameterMapType(parameter)) {
            return Optional.empty();
        }
        if (ACTION_PARAMETER_ANNOTATION.equals(annotationName)) {
            return Optional.of("Unnamed @ActionParameter maps are not supported.");
        }
        if (!(parameter.asType() instanceof DeclaredType declaredType) || declaredType.getTypeArguments().size() != 2) {
            return Optional.of(simpleAnnotationName(annotationName) + " maps must be declared as Map<String, simple value>.");
        }
        TypeMirror keyType = declaredType.getTypeArguments().get(0);
        TypeMirror valueType = declaredType.getTypeArguments().get(1);
        if (!isStringType(keyType)) {
            return Optional.of(simpleAnnotationName(annotationName) + " maps must use String keys.");
        }
        if (!isSimpleParameterType(valueType)) {
            return Optional.of(simpleAnnotationName(annotationName) + " maps support only simple value types.");
        }
        return Optional.empty();
    }

    private String simpleParameterError(String annotationName) {
        return simpleAnnotationName(annotationName) + " supports only simple values. Use @FormData for complex objects.";
    }

    private String simpleAnnotationName(String annotationName) {
        return "@" + annotationName.substring(annotationName.lastIndexOf('.') + 1);
    }

    private boolean isUnnamedParameterMapType(VariableElement parameter) {
        TypeElement mapType = processingEnv.getElementUtils().getTypeElement("java.util.Map");
        return mapType != null && processingEnv.getTypeUtils().isAssignable(
                processingEnv.getTypeUtils().erasure(parameter.asType()),
                processingEnv.getTypeUtils().erasure(mapType.asType()));
    }

    private boolean isStringType(TypeMirror type) {
        TypeElement typeElement = asTypeElement(type);
        return typeElement != null && typeElement.getQualifiedName().contentEquals("java.lang.String");
    }

    private boolean isSimpleParameterType(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return type.getKind() != TypeKind.VOID;
        }
        TypeElement typeElement = asTypeElement(type);
        if (typeElement == null) {
            return false;
        }
        String typeName = typeElement.getQualifiedName().toString();
        if (Set.of(
                "java.lang.String",
                "java.lang.Boolean",
                "java.lang.Character",
                "java.time.LocalDate"
        ).contains(typeName)) {
            return true;
        }
        if (typeElement.getKind() == ElementKind.ENUM) {
            return true;
        }
        TypeElement numberType = processingEnv.getElementUtils().getTypeElement("java.lang.Number");
        return numberType != null
                && processingEnv.getTypeUtils().isAssignable(
                processingEnv.getTypeUtils().erasure(type),
                processingEnv.getTypeUtils().erasure(numberType.asType()));
    }

    private void collectDataMethodNames(ExecutableElement method,
                                        Map<String, TemplateDataModel> modelData,
                                        Map<String, TemplateDataModel> formData) {
        Optional<String> modelDataName = modelDataName(method);
        Optional<String> formDataName = annotationStringValue(method, FORM_DATA_ANNOTATION, "value");
        modelDataName.map(value -> dataName(method, value))
                .ifPresent(name -> modelData.put(name, dataModel(name, method.getReturnType())));
        formDataName.map(value -> dataName(method, value))
                .ifPresent(name -> formData.put(name, dataModel(name, method.getReturnType())));
    }

    private Optional<String> modelDataName(ExecutableElement method) {
        if (!hasAnnotation(method, MODEL_DATA_ANNOTATION)) {
            return Optional.empty();
        }
        var value = annotationStringValue(method, MODEL_DATA_ANNOTATION, "value").orElse("");
        var varName = annotationStringValue(method, MODEL_DATA_ANNOTATION, "varName").orElse("");
        if (!value.isEmpty() && !varName.isEmpty() && !value.equals(varName)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "@ModelData value and varName must be equal when both are set.", method);
        }
        return Optional.of(value.isEmpty() ? varName : value);
    }

    private TemplateDataModel dataModel(String name, TypeMirror type) {
        return dataModel(name, type, MAX_PROPERTY_DEPTH);
    }

    private TemplateDataModel dataModel(String name, TypeMirror type, int depth) {
        if (depth <= 0) {
            return new TemplateDataModel(name, Map.of(), null);
        }
        return new TemplateDataModel(name, fieldsFor(type, depth), elementModelFor(type, depth));
    }

    private TemplateDataModel elementModelFor(TypeMirror type, int depth) {
        if (type instanceof ArrayType arrayType) {
            return dataModel("element", arrayType.getComponentType(), depth - 1);
        }
        TypeElement iterableType = processingEnv.getElementUtils().getTypeElement("java.lang.Iterable");
        if (iterableType == null || !(type instanceof DeclaredType declaredType)) {
            return null;
        }
        if (!processingEnv.getTypeUtils().isAssignable(processingEnv.getTypeUtils().erasure(type), processingEnv.getTypeUtils().erasure(iterableType.asType()))) {
            return null;
        }
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments.isEmpty()) {
            return null;
        }
        return dataModel("element", typeArguments.get(0), depth - 1);
    }

    private Set<String> fieldsFor(TypeMirror type) {
        return fieldsFor(type, MAX_PROPERTY_DEPTH).keySet();
    }

    private Map<String, TemplateDataModel> fieldsFor(TypeMirror type, int depth) {
        TypeElement typeElement = asTypeElement(type);
        if (typeElement == null) {
            return Map.of("value", new TemplateDataModel("value", Map.of(), null));
        }
        Map<String, TemplateDataModel> fields = new LinkedHashMap<>();
        collectTypeFields(typeElement, fields, depth);
        if (fields.isEmpty()) {
            fields.put("value", new TemplateDataModel("value", Map.of(), null));
        }
        return fields;
    }

    private void collectTypeFields(TypeElement type, Map<String, TemplateDataModel> fields, int depth) {
        if (type == null || "java.lang.Object".contentEquals(type.getQualifiedName())) {
            return;
        }
        collectRecordFields(type, fields, depth);
        collectMemberFields(type, fields, depth);
        collectBeanProperties(type, fields, depth);
        TypeElement superType = asTypeElement(type.getSuperclass());
        collectTypeFields(superType, fields, depth);
    }

    private void collectRecordFields(TypeElement type, Map<String, TemplateDataModel> fields, int depth) {
        for (RecordComponentElement component : type.getRecordComponents()) {
            String name = component.getSimpleName().toString();
            fields.putIfAbsent(name, dataModel(name, component.asType(), depth - 1));
        }
    }

    private void collectMemberFields(TypeElement type, Map<String, TemplateDataModel> fields, int depth) {
        for (Element enclosedElement : type.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD && !enclosedElement.getModifiers().contains(Modifier.STATIC)) {
                String name = enclosedElement.getSimpleName().toString();
                fields.putIfAbsent(name, dataModel(name, enclosedElement.asType(), depth - 1));
            }
        }
    }

    private void collectBeanProperties(TypeElement type, Map<String, TemplateDataModel> fields, int depth) {
        for (Element enclosedElement : type.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) enclosedElement;
                propertyName(method).ifPresent(name -> fields.putIfAbsent(name, dataModel(name, propertyType(method), depth - 1)));
            }
        }
    }

    private TypeMirror propertyType(ExecutableElement method) {
        if (method.getParameters().size() == 1) {
            VariableElement parameter = method.getParameters().get(0);
            return parameter.asType();
        }
        return method.getReturnType();
    }

    private Optional<String> propertyName(ExecutableElement method) {
        if (method.getModifiers().contains(Modifier.STATIC)) {
            return Optional.empty();
        }
        if (method.getReturnType().getKind() == TypeKind.VOID && method.getParameters().isEmpty()) {
            return Optional.empty();
        }
        String methodName = method.getSimpleName().toString();
        if (method.getParameters().isEmpty() && methodName.startsWith("get") && methodName.length() > 3) {
            return Optional.of(decapitalize(methodName.substring(3)));
        }
        if (method.getParameters().isEmpty() && methodName.startsWith("is") && methodName.length() > 2) {
            return Optional.of(decapitalize(methodName.substring(2)));
        }
        if (method.getParameters().size() == 1 && methodName.startsWith("set") && methodName.length() > 3) {
            return Optional.of(decapitalize(methodName.substring(3)));
        }
        return Optional.empty();
    }

    private TypeElement asTypeElement(TypeMirror type) {
        if (type instanceof DeclaredType declaredType && declaredType.asElement() instanceof TypeElement typeElement) {
            return typeElement;
        }
        return null;
    }

    private String dataName(ExecutableElement method, String annotationValue) {
        if (annotationValue != null && !annotationValue.isBlank()) {
            return annotationValue;
        }
        String methodName = method.getSimpleName().toString();
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return decapitalize(methodName.substring(3));
        }
        return methodName;
    }

    private String decapitalize(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
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

    private boolean hasAnnotation(Element element, String annotationName) {
        for (var mirror : element.getAnnotationMirrors()) {
            var annotationElement = (TypeElement) mirror.getAnnotationType().asElement();
            if (annotationElement.getQualifiedName().contentEquals(annotationName)) {
                return true;
            }
        }
        return false;
    }
}
