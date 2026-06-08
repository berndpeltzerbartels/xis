package one.xis.processor;

import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generates a runtime runner when a type annotated with @one.xis.boot.XISBootApplication
 * or @one.xis.http.XISHttpApplication is present.
 */
@AutoService(Processor.class)
public class XISBootRunnerProcessor extends AbstractProcessor {

    private static final String ANN_XIS_BOOT_APPLICATION = "one.xis.boot.XISBootApplication";
    private static final String ANN_XIS_HTTP_APPLICATION = "one.xis.http.XISHttpApplication";
    private static final String RUNNER_SIMPLE = "Runner";

    private Elements elements;
    private boolean generated;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elements = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(ANN_XIS_BOOT_APPLICATION, ANN_XIS_HTTP_APPLICATION);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (generated || roundEnv.processingOver()) return false;

        Optional<ApplicationType> appType = findApplicationType(roundEnv);
        if (appType.isEmpty()) return false;

        try {
            generateRunner(appType.get());
            generated = true;
        } catch (IOException ex) {
            error("Generation failed: " + ex.getMessage());
        }

        return false;
    }

    private Optional<ApplicationType> findApplicationType(RoundEnvironment roundEnv) {
        Optional<TypeElement> bootApp = findSingleApplicationType(roundEnv, ANN_XIS_BOOT_APPLICATION);
        Optional<TypeElement> httpApp = findSingleApplicationType(roundEnv, ANN_XIS_HTTP_APPLICATION);
        if (bootApp.isPresent() && httpApp.isPresent()) {
            error("Use either @XISBootApplication or @XISHttpApplication, not both");
            return Optional.empty();
        }
        if (bootApp.isPresent()) {
            return Optional.of(new ApplicationType(bootApp.get(), "one.xis.boot.Runner", "one.xis.boot",
                    "one.xis.boot.XISBootRunner", "@XISBootApplication"));
        }
        return httpApp.map(type -> new ApplicationType(type, "one.xis.http.Runner", "one.xis.http",
                "one.xis.http.XISHttpRunner", "@XISHttpApplication"));
    }

    private Optional<TypeElement> findSingleApplicationType(RoundEnvironment roundEnv, String annotationName) {
        TypeElement ann = elements.getTypeElement(annotationName);
        if (ann == null) return Optional.empty();

        Set<TypeElement> candidates = roundEnv.getElementsAnnotatedWith(ann).stream()
                .filter(e -> e instanceof TypeElement)
                .map(e -> (TypeElement) e)
                .collect(Collectors.toSet());

        return switch (candidates.size()) {
            case 0 -> Optional.empty();
            case 1 -> Optional.of(candidates.iterator().next());
            default -> {
                error("Multiple " + ann.getSimpleName() + " types found");
                yield Optional.empty();
            }
        };
    }

    private void generateRunner(ApplicationType appType) throws IOException {
        String src = buildRunnerSource(appType);
        writeJava(appType.runnerFqcn(), src, appType.applicationClass());
    }

    private String buildRunnerSource(ApplicationType appType) {
        String pkg = appType.runnerPackage().isEmpty() ? "" : "package " + appType.runnerPackage() + ";\n\n";
        return new StringBuilder()
                .append(pkg)
                .append("public final class ").append(RUNNER_SIMPLE).append(" {\n")
                .append("  private ").append(RUNNER_SIMPLE).append("() {}\n")
                .append("  public static void main(String[] args) {\n")
                .append("    ").append(appType.internalRunnerFqcn())
                .append(".run(").append(appType.applicationClass().getQualifiedName()).append(".class, args);\n")
                .append("  }\n")
                .append("}\n")
                .toString();
    }

    private void writeJava(String fqcn, String source, Element... originating) throws IOException {
        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(fqcn, originating);
            try (Writer w = file.openWriter()) {
                w.write(source);
            }
        } catch (javax.annotation.processing.FilerException ignoreIfExists) {
            // already generated by a prior round
        }
    }

    private void warn(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg);
    }

    private void error(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
    }

    private record ApplicationType(TypeElement applicationClass, String runnerFqcn, String runnerPackage,
                                   String internalRunnerFqcn, String annotationName) {
    }
}
