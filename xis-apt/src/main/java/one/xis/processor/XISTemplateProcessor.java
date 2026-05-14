package one.xis.processor;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

@SupportedOptions({"xis.outputDir"})
@AutoService(Processor.class)
public class XISTemplateProcessor extends AbstractProcessor {

    /* ---- options & markers ---- */
    private static final String OPT_OUT = "xis.outputDir";
    private static final String THEME_MARKER_FQCN = "one.xis.theme.Identifier";

    /* ---- annotations ---- */
    private static final String ANN_PAGE = "one.xis.Page";
    private static final String ANN_FRONTLET = "one.xis.Frontlet";
    private static final String ANN_MODEL_DATA = "one.xis.ModelData";
    private static final String ANN_FORM_DATA = "one.xis.FormData";
    private static final String ANN_ACTION = "one.xis.Action";

    /* ---- template locations (inside processor JAR) ---- */
    private static final String PREFIX_DEFAULT = "META-INF/xis/templates/";
    private static final String PREFIX_THEME = "META-INF/xis/templates/xis-theme/";
    private static final String FILE_PAGE = "Page-Template.html";
    private static final String FILE_FRONTLET = "Frontlet-Template.html";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(ANN_PAGE, ANN_FRONTLET);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annos, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) return false;

        Path outDir = resolveOutDir();
        if (outDir == null) return false;

        boolean themePresent = isThemePresent();

        generateGroup(roundEnv, ANN_PAGE, TemplateKind.PAGE, outDir, themePresent);
        generateGroup(roundEnv, ANN_FRONTLET, TemplateKind.FRONTLET, outDir, themePresent);

        return false; // allow others
    }

    /* ====================================================================== */
    /*                               Core                                     */
    /* ====================================================================== */

    private enum TemplateKind {PAGE, FRONTLET}

    private void generateGroup(RoundEnvironment env,
                               String annoFqcn,
                               TemplateKind kind,
                               Path outRoot,
                               boolean themePresent) {
        for (TypeElement type : findAnnotatedTypes(env, annoFqcn)) {
            generateIfMissing(type, kind, outRoot, themePresent);
        }
    }

    private void generateIfMissing(TypeElement type,
                                   TemplateKind kind,
                                   Path outRoot,
                                   boolean themePresent) {
        Path target = targetPath(outRoot, type);
        if (Files.exists(target)) return; // never overwrite

        String template = loadTemplate(kind, themePresent);
        if (template == null) template = minimalTemplate(type.getSimpleName().toString(), kind);
        template = applyControllerData(template, kind, describeController(type));

        writeFile(target, template);
    }

    private String applyControllerData(String template, TemplateKind kind, ControllerTemplateDescriptor descriptor) {
        String content = generatedContent(descriptor);
        if (content.isBlank()) {
            return template;
        }
        if (kind == TemplateKind.PAGE) {
            return insertBeforeClosingTag(template, "body", content);
        }
        return insertBeforeClosingTag(template, "xis:template", content);
    }

    private String insertBeforeClosingTag(String template, String tagName, String content) {
        String closingTag = "</" + tagName + ">";
        int index = template.toLowerCase(Locale.ROOT).lastIndexOf(closingTag.toLowerCase(Locale.ROOT));
        if (index < 0) {
            return template + System.lineSeparator() + content;
        }
        return template.substring(0, index) + content + System.lineSeparator() + template.substring(index);
    }

    private String generatedContent(ControllerTemplateDescriptor descriptor) {
        StringBuilder content = new StringBuilder();
        for (TemplateModelData modelData : descriptor.modelData()) {
            appendModelData(content, modelData);
        }
        for (TemplateFormData formData : descriptor.formData()) {
            appendFormData(content, formData);
        }
        appendActions(content, descriptor.actions());
        return content.toString();
    }

    private void appendModelData(StringBuilder content, TemplateModelData modelData) {
        content.append("\n<section>\n");
        if (modelData.iterable()) {
            String itemName = singularName(modelData.name());
            content.append("  <ul xis:foreach=\"")
                    .append(itemName)
                    .append(":${")
                    .append(modelData.name())
                    .append("}\">\n")
                    .append("    <li>${")
                    .append(itemName)
                    .append("}</li>\n")
                    .append("  </ul>\n");
        } else {
            content.append("  <div>${")
                    .append(modelData.name())
                    .append("}</div>\n");
        }
        content.append("</section>\n");
    }

    private void appendFormData(StringBuilder content, TemplateFormData formData) {
        content.append("\n<form xis:binding=\"")
                .append(formData.name())
                .append("\">\n")
                .append("  <xis:global-messages/>\n");
        for (TemplateField field : formData.fields()) {
            appendFormField(content, formData.name(), field);
        }
        content.append("  <button type=\"button\" xis:action=\"")
                .append(formData.actionName())
                .append("\">Save</button>\n")
                .append("</form>\n");
    }

    private void appendActions(StringBuilder content, List<TemplateAction> actions) {
        if (actions.isEmpty()) {
            return;
        }
        content.append("\n<nav>\n");
        for (TemplateAction action : actions) {
            content.append("  <button type=\"button\" xis:action=\"")
                    .append(action.name())
                    .append("\">")
                    .append(label(action.name()))
                    .append("</button>\n");
        }
        content.append("</nav>\n");
    }

    private void appendFormField(StringBuilder content, String formName, TemplateField field) {
        String id = formName + "-" + field.name();
        content.append("  <div>\n")
                .append("    <label for=\"")
                .append(id)
                .append("\" xis:error-binding=\"")
                .append(field.name())
                .append("\" xis:error-class=\"error\">")
                .append(label(field.name()))
                .append("</label>\n")
                .append("    <input id=\"")
                .append(id)
                .append("\" type=\"")
                .append(field.inputType())
                .append("\" xis:binding=\"")
                .append(field.name())
                .append("\" xis:error-class=\"error\"/>\n")
                .append("    <div xis:message-for=\"")
                .append(field.name())
                .append("\"></div>\n")
                .append("  </div>\n");
    }

    /* ====================================================================== */
    /*                            Template loading                             */
    /* ====================================================================== */

    private String loadTemplate(TemplateKind kind, boolean themePresent) {
        String file = (kind == TemplateKind.PAGE) ? FILE_PAGE : FILE_FRONTLET;

        // Theme-first, then default
        List<String> candidates = new ArrayList<>(2);
        if (themePresent) candidates.add(PREFIX_THEME + file);
        candidates.add(PREFIX_DEFAULT + file);

        for (String path : candidates) {
            String s = readResource(path);
            if (s != null) return s;
        }
        return null;
    }

    private String readResource(String path) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            if (in == null) return null;
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean isThemePresent() {
        // Compiler-Symboltabelle nutzen (keine Class.forName): zuverlässig auf dem Compile-Classpath
        return processingEnv.getElementUtils().getTypeElement(THEME_MARKER_FQCN) != null;
    }

    private ControllerTemplateDescriptor describeController(TypeElement type) {
        List<TemplateModelData> modelData = new ArrayList<>();
        List<TemplateFormData> formData = new ArrayList<>();
        Set<String> formActionNames = new LinkedHashSet<>();
        for (Element enclosedElement : type.getEnclosedElements()) {
            if (enclosedElement.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement method = (ExecutableElement) enclosedElement;
            collectModelData(method, modelData);
            collectFormData(type, method, formData, formActionNames);
        }
        return new ControllerTemplateDescriptor(modelData, formData, actions(type, formActionNames));
    }

    private void collectModelData(ExecutableElement method, List<TemplateModelData> modelData) {
        findAnnotationStringValue(method, ANN_MODEL_DATA, "value")
                .map(value -> dataName(method, value))
                .ifPresent(name -> modelData.add(new TemplateModelData(name, isIterable(method.getReturnType()))));
    }

    private void collectFormData(TypeElement controllerType,
                                 ExecutableElement method,
                                 List<TemplateFormData> formData,
                                 Set<String> formActionNames) {
        findAnnotationStringValue(method, ANN_FORM_DATA, "value")
                .map(value -> dataName(method, value))
                .ifPresent(name -> addFormData(controllerType, method, formData, formActionNames, name));
    }

    private void addFormData(TypeElement controllerType,
                             ExecutableElement method,
                             List<TemplateFormData> formData,
                             Set<String> formActionNames,
                             String name) {
        String actionName = actionNameForForm(controllerType, name);
        formActionNames.add(actionName);
        formData.add(new TemplateFormData(name, actionName, fieldsFor(method.getReturnType())));
    }

    private List<TemplateAction> actions(TypeElement controllerType, Set<String> excludedActionNames) {
        List<TemplateAction> actions = new ArrayList<>();
        for (Element enclosedElement : controllerType.getEnclosedElements()) {
            if (enclosedElement.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement method = (ExecutableElement) enclosedElement;
            action(method)
                    .filter(action -> !excludedActionNames.contains(action.name()))
                    .ifPresent(actions::add);
        }
        return actions;
    }

    private Optional<TemplateAction> action(ExecutableElement method) {
        return findAnnotationStringValue(method, ANN_ACTION, "value")
                .map(value -> value.isBlank() ? method.getSimpleName().toString() : value)
                .map(TemplateAction::new);
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

    private String actionNameForForm(TypeElement controllerType, String formName) {
        for (Element enclosedElement : controllerType.getEnclosedElements()) {
            if (enclosedElement.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement method = (ExecutableElement) enclosedElement;
            if (isActionForForm(method, formName)) {
                return actionName(method);
            }
        }
        return "save";
    }

    private boolean isActionForForm(ExecutableElement method, String formName) {
        if (findAnnotationStringValue(method, ANN_ACTION, "value").isEmpty()) {
            return false;
        }
        for (VariableElement parameter : method.getParameters()) {
            Optional<String> formDataName = findAnnotationStringValue(parameter, ANN_FORM_DATA, "value");
            if (formDataName.isPresent() && formName.equals(formDataName.get())) {
                return true;
            }
        }
        return false;
    }

    private String actionName(ExecutableElement method) {
        return findAnnotationStringValue(method, ANN_ACTION, "value")
                .filter(value -> !value.isBlank())
                .orElse(method.getSimpleName().toString());
    }

    private List<TemplateField> fieldsFor(TypeMirror type) {
        TypeElement typeElement = asTypeElement(type);
        if (typeElement == null) {
            return List.of(new TemplateField("value", inputType(type)));
        }
        List<TemplateField> fields = recordFields(typeElement);
        if (!fields.isEmpty()) {
            return fields;
        }
        fields = memberFields(typeElement);
        if (!fields.isEmpty()) {
            return fields;
        }
        return List.of(new TemplateField("value", "text"));
    }

    private List<TemplateField> recordFields(TypeElement type) {
        List<TemplateField> fields = new ArrayList<>();
        for (RecordComponentElement component : type.getRecordComponents()) {
            fields.add(new TemplateField(component.getSimpleName().toString(), inputType(component.asType())));
        }
        return fields;
    }

    private List<TemplateField> memberFields(TypeElement type) {
        List<TemplateField> fields = new ArrayList<>();
        for (Element enclosedElement : type.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD && !enclosedElement.getModifiers().contains(Modifier.STATIC)) {
                fields.add(new TemplateField(enclosedElement.getSimpleName().toString(), inputType(enclosedElement.asType())));
            }
        }
        return fields;
    }

    private TypeElement asTypeElement(TypeMirror type) {
        if (type instanceof DeclaredType declaredType && declaredType.asElement() instanceof TypeElement typeElement) {
            return typeElement;
        }
        return null;
    }

    private boolean isIterable(TypeMirror type) {
        if (type.getKind() == TypeKind.ARRAY) {
            return true;
        }
        TypeElement iterableType = processingEnv.getElementUtils().getTypeElement("java.lang.Iterable");
        return iterableType != null
                && type instanceof DeclaredType
                && typeUtils().isAssignable(typeUtils().erasure(type), typeUtils().erasure(iterableType.asType()));
    }

    private Types typeUtils() {
        return processingEnv.getTypeUtils();
    }

    private String inputType(TypeMirror type) {
        String name = type.toString();
        if (type.getKind() == TypeKind.BOOLEAN || "java.lang.Boolean".equals(name)) {
            return "checkbox";
        }
        if (isNumber(type, name)) {
            return "number";
        }
        if (name.endsWith("LocalDate") || name.endsWith(".Date")) {
            return "date";
        }
        return "text";
    }

    private boolean isNumber(TypeMirror type, String name) {
        if (type.getKind().isPrimitive()) {
            return type.getKind() != TypeKind.BOOLEAN && type.getKind() != TypeKind.CHAR;
        }
        return name.startsWith("java.lang.") && Set.of(
                "java.lang.Byte",
                "java.lang.Short",
                "java.lang.Integer",
                "java.lang.Long",
                "java.lang.Float",
                "java.lang.Double"
        ).contains(name);
    }

    private String label(String fieldName) {
        if (fieldName.isBlank()) {
            return fieldName;
        }
        StringBuilder label = new StringBuilder();
        label.append(Character.toUpperCase(fieldName.charAt(0)));
        for (int index = 1; index < fieldName.length(); index++) {
            char c = fieldName.charAt(index);
            if (Character.isUpperCase(c)) {
                label.append(' ');
            }
            label.append(c);
        }
        return label.toString();
    }

    private String singularName(String name) {
        if (name.endsWith("ies") && name.length() > 3) {
            return name.substring(0, name.length() - 3) + "y";
        }
        if (name.endsWith("s") && name.length() > 1) {
            return name.substring(0, name.length() - 1);
        }
        return "item";
    }

    /* ====================================================================== */
    /*                                IO & paths                               */
    /* ====================================================================== */


    private Path targetPath(Path outRoot, TypeElement type) {
        // 1) Optionaler Override via @one.xis.HtmlFile(value = "..."):
        Optional<String> override = resolveHtmlFileOverride(type);
        if (override.isPresent()) {
            return targetPathForHtmlFileOverride(outRoot, type, override.get());
        }

        // 2) Default: Paketpfad + SimpleName.html
        String pkg = processingEnv.getElementUtils()
                .getPackageOf(type)
                .getQualifiedName()
                .toString();
        String file = type.getSimpleName() + ".html";
        Path base = pkg.isEmpty() ? outRoot : outRoot.resolve(pkg.replace('.', '/'));
        return base.resolve(file);
    }

    /* ----------------- helpers ----------------- */

    /**
     * Liest den Stringwert von @one.xis.HtmlFile(value=...), ohne harte Abhängigkeit auf die Annotation-Klasse.
     */
    private Optional<String> resolveHtmlFileOverride(TypeElement type) {
        return findAnnotationStringValue(type, "one.xis.HtmlFile", "value");
    }

    /**
     * Sucht ein AnnotationMirror per FQCN und liefert den Stringwert des gewünschten Elements.
     * Beachtet auch Default-Werte, falls das Element nicht explizit gesetzt wurde.
     */
    private Optional<String> findAnnotationStringValue(Element type, String annotationFqcn, String elementName) {
        for (var mirror : type.getAnnotationMirrors()) {
            var annType = mirror.getAnnotationType();
            var annElement = (TypeElement) annType.asElement();
            if (!annElement.getQualifiedName().contentEquals(annotationFqcn)) continue;

            // Map der explizit gesetzten Werte:
            var values = mirror.getElementValues();

            // Gesuchten Methodenslot finden:
            for (var method : annElement.getEnclosedElements()) {
                if (method.getKind() != ElementKind.METHOD) continue;
                var exec = (ExecutableElement) method;
                if (!exec.getSimpleName().contentEquals(elementName)) continue;

                // explizit gesetzter Wert?
                var explicit = values.get(exec);
                if (explicit != null) {
                    Object v = explicit.getValue();
                    return Optional.ofNullable(v).map(Object::toString);
                }
                // Default-Wert verwenden (falls vorhanden)
                var def = exec.getDefaultValue();
                if (def != null) {
                    Object v = def.getValue();
                    return Optional.ofNullable(v).map(Object::toString);
                }
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * Normalisiert einen (relativen) Pfad: Trim, Backslashes → '/', führende '/' entfernen.
     */
    private String normalizeRelativePath(String p) {
        String s = p == null ? "" : p.trim().replace('\\', '/');
        while (s.startsWith("/")) s = s.substring(1);
        return s;
    }

    private Path targetPathForHtmlFileOverride(Path outRoot, TypeElement type, String override) {
        boolean absolute = override != null && override.trim().replace('\\', '/').startsWith("/");
        String rel = normalizeRelativePath(override);
        if (absolute) {
            return outRoot.resolve(rel);
        }
        String pkg = processingEnv.getElementUtils()
                .getPackageOf(type)
                .getQualifiedName()
                .toString();
        Path base = pkg.isEmpty() ? outRoot : outRoot.resolve(pkg.replace('.', '/'));
        return base.resolve(rel);
    }


    private void writeFile(Path target, String content) {
        try {
            Files.createDirectories(target.getParent());
            Files.writeString(target, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        } catch (IOException ignored) {
            // silent by design
        }
    }

    private Path resolveOutDir() {
        String val = processingEnv.getOptions().get(OPT_OUT);
        return (val == null || val.isBlank()) ? null : Paths.get(val);
    }

    /* ====================================================================== */
    /*                           Utilities (model)                             */
    /* ====================================================================== */

    private Set<TypeElement> findAnnotatedTypes(RoundEnvironment env, String annotationFqcn) {
        TypeElement ann = processingEnv.getElementUtils().getTypeElement(annotationFqcn);
        if (ann == null) return Collections.emptySet();

        Set<TypeElement> result = new LinkedHashSet<>();
        for (Element e : env.getElementsAnnotatedWith(ann)) {
            if (e.getKind() == ElementKind.CLASS) {
                result.add((TypeElement) e);
            }
        }
        return result;
    }

    private String minimalTemplate(String title, TemplateKind kind) {
        if (kind == TemplateKind.PAGE) {
            return """
                    <!DOCTYPE html>
                    <html xmlns:xis="https://xis.one/xsd">
                      <head><meta charset="utf-8"><title>%s</title></head>
                      <body><h1>%s</h1><!-- @generated --></body>
                    </html>
                    """.formatted(title, title);
        } else {
            // Frontlet: generate fragment
            return """
                    <xis:template xmlns:xis="https://xis.one/xsd">
                        <div><!-- @generated --></div>
                    </xis:template>
                    """;
        }
    }
}
