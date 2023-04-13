package spring.test;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IndexTest {

    IntegrationTestContext testContext;

    @BeforeEach
    void createContext() {
        testContext = IntegrationTestContext.builder()
                .withSingelton(new Index())
                .build();
    }

    @Test
    void showIndex() {
        testContext.openPage("/index.html");
    }

}
