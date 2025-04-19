package one.xis.server;

import org.junit.jupiter.api.Test;

class ImportedTypesTest {

    @Test
    void init() {
        long t0 = System.currentTimeMillis();
        ImportedTypes.getImportedTypes();
        long t1 = System.currentTimeMillis();
        System.out.println("Elapsed time: " + (t1 - t0) + "ms");
    }
}