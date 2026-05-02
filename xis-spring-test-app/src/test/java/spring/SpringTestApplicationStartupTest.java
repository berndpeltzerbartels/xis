package spring;

import one.xis.spring.SpringContextAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringTestApplicationStartupTest {

    @Test
    void startsSpringApplicationWithXisAdapter() {
        try (var context = new SpringApplicationBuilder(SpringTestApplication.class)
                .properties(
                        "server.port=0",
                        "spring.main.banner-mode=off"
                )
                .run()) {
            assertTrue(context.isRunning());
            assertNotNull(context.getBean(SpringContextAdapter.class));
        }
    }
}
