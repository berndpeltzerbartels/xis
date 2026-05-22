package one.xis.distributed;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DistributedHostsControllerTest {

    @Test
    void returnsConfiguredHosts() {
        var controller = new DistributedHostsController(() -> List.of(
                "https://shop.example.com",
                "http://catalog.example.com:9000"));

        var response = controller.getHosts();

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody())
                .containsExactly("https://shop.example.com", "http://catalog.example.com:9000");
    }
}
