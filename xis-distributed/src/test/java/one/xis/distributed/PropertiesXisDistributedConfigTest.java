package one.xis.distributed;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesXisDistributedConfigTest {

    @Test
    void readsExplicitWidgetAndPageMappings() {
        var config = new PropertiesXisDistributedConfig();

        assertThat(config.isRemoteWidget("ProductWidget")).isTrue();
        assertThat(config.getWidgetHost("ProductWidget")).isEqualTo("https://shop.example.com");

        assertThat(config.isRemotePage("/product/*.html")).isTrue();
        assertThat(config.getPageHost("/product/*.html")).isEqualTo("https://shop.example.com");
    }

    @Test
    void leavesUnmappedComponentsLocal() {
        var config = new PropertiesXisDistributedConfig();

        assertThat(config.isRemoteWidget("LocalWidget")).isFalse();
        assertThat(config.getWidgetHost("LocalWidget")).isNull();

        assertThat(config.isRemotePage("/local.html")).isFalse();
        assertThat(config.getPageHost("/local.html")).isNull();
    }
}
