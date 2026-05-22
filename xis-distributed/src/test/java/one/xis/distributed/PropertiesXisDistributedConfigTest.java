package one.xis.distributed;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesXisDistributedConfigTest {

    @Test
    void readsConfiguredHosts() {
        var config = new PropertiesXisDistributedConfig();

        assertThat(config.getHosts())
                .containsExactly("https://shop.example.com", "http://catalog.example.com:9000");
    }
}
