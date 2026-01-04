package one.xis.processor;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Annotation processor that generates test files for @Page controllers.
 * Creates integration tests using IntegrationTestContext.
 */
@SupportedOptions({"xis.testOutputDir"})
@AutoService(Processor.class)
public class XISTestProcessor extends AbstractProcessor {

    /* ---- options ---- */
    private static final String OPT_TEST_OUT = "xis.testOutputDir";

    /* ---- annotations ---- */
    private static final String ANN_PAGE = "one.xis.Page";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(ANN_PAGE);
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

        generateTests(roundEnv, ANN_PAGE, outDir);

        return false; // allow others
    }

    /* ====================================================================== */
    /*                               Core                                     */
    /* ====================================================================== */

    private void generateTests(RoundEnvironment env, String annoFqcn, Path outRoot) {
        for (TypeElement type : findAnnotatedTypes(env, annoFqcn)) {
            generateTestIfMissing(type, outRoot);
        }
    }

    private void generateTestIfMissing(TypeElement type, Path outRoot) {
        Path target = targetPath(outRoot, type);
        if (Files.exists(target)) return; // never overwrite

        String testContent = generateTestContent(type);
        writeFile(target, testContent);
    }

    /* ====================================================================== */
    /*                          Test generation                                */
    /* ====================================================================== */

    private String generateTestContent(TypeElement type) {
        String testPackage = processingEnv.getElementUtils()
                .getPackageOf(type)
                .getQualifiedName()
                .toString();

        String typePackage = processingEnv.getElementUtils()
                .getPackageOf(type)
                .getQualifiedName()
                .toString();

        String className = type.getSimpleName().toString();
        String testClassName = className + "Test";

        // Extract page URL from @Page annotation
        Optional<String> pageUrl = findAnnotationStringValue(type, ANN_PAGE, "value");
        String url = pageUrl.orElse("/unknown.html");

        // Check if we need an import for the controller class
        boolean needsImport = !testPackage.equals(typePackage);

        StringBuilder sb = new StringBuilder();

        // Package declaration
        if (!testPackage.isEmpty()) {
            sb.append("package ").append(testPackage).append(";\n\n");
        }

        // Imports
        sb.append("import one.xis.context.IntegrationTestContext;\n");
        sb.append("import org.junit.jupiter.api.BeforeEach;\n");
        sb.append("import org.junit.jupiter.api.Test;\n\n");
        sb.append("import static org.assertj.core.api.Assertions.assertThat;\n");

        // Add controller import if needed
        if (needsImport) {
            sb.append("\nimport ").append(type.getQualifiedName()).append(";\n");
        }

        sb.append("\n");

        // Class declaration
        sb.append("/**\n");
        sb.append(" * Integration test for ").append(className).append("\n");
        sb.append(" * @generated\n");
        sb.append(" */\n");
        sb.append("class ").append(testClassName).append(" {\n\n");

        // Field
        sb.append("    private IntegrationTestContext context;\n\n");

        // setUp method
        sb.append("    @BeforeEach\n");
        sb.append("    void setUp() {\n");
        sb.append("        context = IntegrationTestContext.builder()\n");
        sb.append("            .withSingleton(").append(className).append(".class)\n");
        sb.append("            .build();\n");
        sb.append("    }\n\n");

        // Test method - use URL instead of class
        sb.append("    @Test\n");
        sb.append("    void test() {\n");
        sb.append("        // Edit url if you need path variables or query parameters\n");
        sb.append("        var result = context.openPage(\"").append(url).append("\");\n");
        sb.append("        var document = result.getDocument();\n\n");
        sb.append("        assertThat(document).isNotNull();\n");
        sb.append("        // TODO: Add more assertions\n");
        sb.append("    }\n");

        sb.append("}\n");

        return sb.toString();
    }

    /* ====================================================================== */
    /*                          Annotation reading                             */
    /* ====================================================================== */

    /**
     * Reads the String value from an annotation element.
     */
    private Optional<String> findAnnotationStringValue(TypeElement type, String annotationFqcn, String elementName) {
        for (var mirror : type.getAnnotationMirrors()) {
            var annType = mirror.getAnnotationType();
            var annElement = (TypeElement) annType.asElement();
            if (!annElement.getQualifiedName().contentEquals(annotationFqcn)) continue;

            // Map of explicitly set values:
            var values = mirror.getElementValues();

            // Find the method slot:
            for (var method : annElement.getEnclosedElements()) {
                if (method.getKind() != ElementKind.METHOD) continue;
                var exec = (ExecutableElement) method;
                if (!exec.getSimpleName().contentEquals(elementName)) continue;

                // explicitly set value?
                var explicit = values.get(exec);
                if (explicit != null) {
                    Object v = explicit.getValue();
                    return Optional.ofNullable(v).map(Object::toString);
                }
                // Default value (if exists)
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

    /* ====================================================================== */
    /*                                IO & paths                               */
    /* ====================================================================== */

    private Path targetPath(Path outRoot, TypeElement type) {
        String pkg = processingEnv.getElementUtils()
                .getPackageOf(type)
                .getQualifiedName()
                .toString();

        String testFile = type.getSimpleName() + "Test.java";
        Path base = pkg.isEmpty() ? outRoot : outRoot.resolve(pkg.replace('.', '/'));
        return base.resolve(testFile);
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
        String val = processingEnv.getOptions().get(OPT_TEST_OUT);
        return (val == null || val.isBlank()) ? null : Paths.get(val);
    }

    /* ====================================================================== */
    /*                           Utilities                                     */
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
}
