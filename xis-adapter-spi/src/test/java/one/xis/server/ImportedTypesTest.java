package one.xis.server;

import one.xis.Frontlet;
import one.xis.Formatter;
import one.xis.Page;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImportedTypesTest {

    @Test
    void importedTypesAndAnnotationsAreSeparated() {
        long t0 = System.currentTimeMillis();
        var importedTypes = ImportedTypes.getImportedTypes();
        var importedAnnotations = ImportedTypes.getImportedAnnotations();
        long t1 = System.currentTimeMillis();
        System.out.println("Elapsed time: " + (t1 - t0) + "ms");

        assertThat(importedTypes).contains(Formatter.class);
        assertThat(importedTypes).doesNotContain(Page.class, Frontlet.class);
        assertThat(importedAnnotations).contains(Page.class, Frontlet.class);
    }
}
