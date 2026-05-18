package one.xis.boot.http;

import one.xis.boot.XISBootApplication;
import one.xis.boot.XISBootRunner;
import one.xis.http.Controller;
import one.xis.http.Get;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XisBootHttpDependencyTest {

    @Test
    void dependencyExposesBootAndHttpControllerApi() {
        assertEquals("ok", new ProbeController().probe());
    }

    @XISBootApplication
    static class Application {

        static void main(String[] args) {
            XISBootRunner.run(Application.class, args);
        }
    }

    @Controller
    static class ProbeController {

        @Get("/probe")
        String probe() {
            return "ok";
        }
    }
}
