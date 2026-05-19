package one.xis.systemtests.infrastructure;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class TestcontainersInfrastructureTest {

    private static final String EXPECTED_BODY = "xis-testcontainers-ok";

    @Container
    static final GenericContainer<?> HTTP_ECHO = new GenericContainer<>(DockerImageName.parse("hashicorp/http-echo:1.0"))
            .withExposedPorts(8080)
            .withCommand("-listen=:8080", "-text=" + EXPECTED_BODY)
            .withStartupTimeout(Duration.ofSeconds(30));

    @Test
    void exposesContainerPortsToTheJvm() throws Exception {
        var uri = URI.create("http://" + HTTP_ECHO.getHost() + ":" + HTTP_ECHO.getMappedPort(8080));
        var request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
                .as("Testcontainers must expose mapped container ports to the JVM. Failed URL: %s", uri)
                .isEqualTo(200);
        assertThat(response.body()).contains(EXPECTED_BODY);
    }
}
