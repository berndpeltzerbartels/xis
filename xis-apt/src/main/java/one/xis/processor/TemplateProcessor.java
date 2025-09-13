package one.xis.processor;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
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
public class TemplateProcessor extends AbstractProcessor {

    /* ---- options & markers ---- */
    private static final String OPT_OUT = "xis.outputDir";
    private static final String THEME_MARKER_FQCN = "one.xis.theme.Identifier";

    /* ---- annotations ---- */
    private static final String ANN_PAGE = "one.xis.Page";
    private static final String ANN_WIDGET = "one.xis.Widget";

    /* ---- template locations (inside processor JAR) ---- */
    private static final String PREFIX_DEFAULT = "META-INF/xis/templates/";
    private static final String PREFIX_THEME = "META-INF/xis/templates/xis-theme/";
    private static final String FILE_PAGE = "Page-Template.html";
    private static final String FILE_WIDGET = "Widget-Template.html";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(ANN_PAGE, ANN_WIDGET);
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
        generateGroup(roundEnv, ANN_WIDGET, TemplateKind.WIDGET, outDir, themePresent);

        return false; // allow others
    }

    /* ====================================================================== */
    /*                               Core                                     */
    /* ====================================================================== */

    private enum TemplateKind {PAGE, WIDGET}

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
        if (template == null) template = minimalTemplate(type.getSimpleName().toString());

        writeFile(target, template);
    }

    /* ====================================================================== */
    /*                            Template loading                             */
    /* ====================================================================== */

    private String loadTemplate(TemplateKind kind, boolean themePresent) {
        String file = (kind == TemplateKind.PAGE) ? FILE_PAGE : FILE_WIDGET;

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

    /* ====================================================================== */
    /*                                IO & paths                               */
    /* ====================================================================== */


    private Path targetPath(Path outRoot, TypeElement type) {
        // 1) Optionaler Override via @one.xis.HtmlFile(value = "..."):
        Optional<String> override = resolveHtmlFileOverride(type);
        if (override.isPresent()) {
            String rel = normalizeRelativePath(override.get());
            return outRoot.resolve(rel);
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
    private Optional<String> findAnnotationStringValue(TypeElement type, String annotationFqcn, String elementName) {
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

    private String minimalTemplate(String title) {
        return """
                <!doctype html>
                <html>
                  <head><meta charset="utf-8"><title>%s</title></head>
                  <body><h1>%s</h1><!-- @generated --></body>
                </html>
                """.formatted(title, title);
    }
}
