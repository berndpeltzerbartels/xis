package one.xis.server;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.xis.ImportInstances;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImportedTypes {

    @Getter
    private static Set<Class<?>> importedTypes;

    static {
        init();
    }

    private static void init() {
        var reflections = new Reflections("one.xis", new TypeAnnotationsScanner(), new SubTypesScanner());
        importedTypes = reflections.getTypesAnnotatedWith(ImportInstances.class);
    }
}
