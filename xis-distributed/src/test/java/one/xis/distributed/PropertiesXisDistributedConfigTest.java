package one.xis.distributed;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesXisDistributedConfigTest {

    @Test
    void readsExplicitFrontletPageAndOriginMappings() {
        var config = new PropertiesXisDistributedConfig();

        assertThat(config.getFrontletHosts()).containsEntry("ProductFrontlet", "https://shop.example.com");
        assertThat(config.getFrontletUrls()).containsEntry("ProductFrontlet", "/product-summary");

        assertThat(config.getPageHosts()).containsEntry("/product/*.html", "https://shop.example.com");

        assertThat(config.getAllowedOrigins())
                .containsExactlyInAnyOrder("https://shop.example.com", "https://app.example.com");
    }

    @Test
    void leavesUnmappedComponentsLocal() {
        var config = new PropertiesXisDistributedConfig();

        assertThat(config.getFrontletHosts()).doesNotContainKey("LocalFrontlet");

        assertThat(config.getPageHosts()).doesNotContainKey("/local.html");
    }
}
