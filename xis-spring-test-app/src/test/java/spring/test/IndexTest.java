package spring.test;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IndexTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void createContext() {
        testContext = IntegrationTestContext.builder()
                .withSingelton(new Index())
                .build();
    }

    @Test
    void showIndex() {
        var document = testContext.getDocument();
        testContext.openPage("/index.html");

        document = testContext.getDocument();
// TODO

    }

}
