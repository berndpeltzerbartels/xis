package one.xis.context.performance;

import lombok.extern.slf4j.Slf4j;
import one.xis.context.AppContextBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
class PerfTest {

    @Test
    void test() {

        var i0 = System.currentTimeMillis();
        var context = AppContextBuilder.createInstance()
                .withPackage("one.xis.context.performance")
                .build();
        var i1 = System.currentTimeMillis();
        var t = i1 - i0;
        log.info("context creation: {} ms", t);
        assertThat(t).isLessThan(200);

    }
}
