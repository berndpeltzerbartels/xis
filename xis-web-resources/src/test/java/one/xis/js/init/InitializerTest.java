package one.xis.js.init;

import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@SuppressWarnings("unchecked")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InitializerTest {

    private String initializer;

    @BeforeAll
    void load() {
        initializer = IOUtils.getResourceAsString("js/init/Initializer.js");
    }

}
