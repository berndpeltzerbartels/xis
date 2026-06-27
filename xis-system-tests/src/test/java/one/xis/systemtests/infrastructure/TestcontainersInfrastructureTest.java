package one.xis.systemtests.infrastructure;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
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

    @Container
    static final GenericContainer<?> HTTP_SERVER = new GenericContainer<>(DockerImageName.parse("nginx:alpine"))
            .withExposedPorts(80)
            .waitingFor(Wait.forHttp("/").forStatusCode(200))
            .withStartupTimeout(Duration.ofSeconds(30));

    @Test
    void exposesContainerPortsToTheJvm() throws Exception {
        var uri = URI.create("http://" + HTTP_SERVER.getHost() + ":" + HTTP_SERVER.getMappedPort(80));
        var request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
                .as("Testcontainers must expose mapped container ports to the JVM. Failed URL: %s", uri)
                .isEqualTo(200);
        assertThat(response.body()).contains("Welcome to nginx");
    }
}
