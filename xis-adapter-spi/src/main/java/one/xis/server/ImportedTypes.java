package one.xis.server;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.xis.ImportInstances;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImportedTypes {

    @Getter
    private static Set<Class<?>> importedTypes;

    @Getter
    private static Set<Class<? extends Annotation>> importedAnnotations;

    static {
        init();
    }

    private static void init() {
        var reflections = new Reflections("one.xis", new TypeAnnotationsScanner(), new SubTypesScanner());
        var imports = reflections.getTypesAnnotatedWith(ImportInstances.class);
        importedTypes = new LinkedHashSet<>();
        importedAnnotations = new LinkedHashSet<>();
        for (var importedType : imports) {
            if (importedType.isAnnotation()) {
                importedAnnotations.add(importedType.asSubclass(Annotation.class));
            } else {
                importedTypes.add(importedType);
            }
        }
    }
}
