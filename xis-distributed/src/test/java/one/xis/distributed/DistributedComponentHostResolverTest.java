package one.xis.distributed;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DistributedComponentHostResolverTest {

    @Test
    void returnsConfiguredFrontletHostFromCachedMap() {
        var config = new MutableConfig();
        config.frontletHosts.put("ScoreFrontlet", "https://frontlets.example.com");

        var resolver = new DistributedComponentHostResolver(config);
        config.frontletHosts.clear();

        assertThat(resolver.getFrontletHost("ScoreFrontlet")).isEqualTo("https://frontlets.example.com");
    }

    @Test
    void returnsConfiguredPageHostFromCachedMap() {
        var config = new MutableConfig();
        config.pageHosts.put("/shop/*.html", "https://pages.example.com");

        var resolver = new DistributedComponentHostResolver(config);
        config.pageHosts.clear();

        assertThat(resolver.getPageHost("/shop/*.html")).isEqualTo("https://pages.example.com");
    }

    @Test
    void returnsNullForLocalComponent() {
        var resolver = new DistributedComponentHostResolver(new MutableConfig());

        assertThat(resolver.getFrontletHost("LocalFrontlet")).isNull();
        assertThat(resolver.getPageHost("/local.html")).isNull();
    }

    @Test
    void exposesConfiguredFrontletUrlsFromCachedMap() {
        var config = new MutableConfig();
        config.frontletHosts.put("ScoreFrontlet", "https://frontlets.example.com");
        config.frontletUrls.put("ScoreFrontlet", "/score");

        var resolver = new DistributedComponentHostResolver(config);
        config.frontletUrls.clear();

        assertThat(resolver.getFrontletUrls()).containsEntry("ScoreFrontlet", "/score");
    }

    @Test
    void exposesConfiguredFrontletHostsFromCachedMap() {
        var config = new MutableConfig();
        config.frontletHosts.put("ScoreFrontlet", "https://frontlets.example.com");

        var resolver = new DistributedComponentHostResolver(config);
        config.frontletHosts.clear();

        assertThat(resolver.getFrontletHosts()).containsEntry("ScoreFrontlet", "https://frontlets.example.com");
    }

    @Test
    void readsUserProvidedMapsOnlyOnce() {
        var config = new CountingConfig();

        var resolver = new DistributedComponentHostResolver(config);

        resolver.getFrontletHost("ScoreFrontlet");
        resolver.getFrontletHost("ScoreFrontlet");
        resolver.getFrontletHosts();
        resolver.getFrontletUrls();
        resolver.getPageHost("/shop/*.html");
        resolver.getPageHost("/shop/*.html");

        assertThat(config.frontletHostCalls).isEqualTo(1);
        assertThat(config.frontletUrlCalls).isEqualTo(1);
        assertThat(config.pageHostCalls).isEqualTo(1);
    }

    @Test
    void rejectsBlankFrontletHostMappingOnceAtStartup() {
        var config = new MutableConfig();
        config.frontletHosts.put("ScoreFrontlet", " ");

        assertThatThrownBy(() -> new DistributedComponentHostResolver(config))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("frontlet 'ScoreFrontlet'");
    }

    @Test
    void rejectsBlankPageHostMappingOnceAtStartup() {
        var config = new MutableConfig();
        config.pageHosts.put("/shop/*.html", " ");

        assertThatThrownBy(() -> new DistributedComponentHostResolver(config))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("page '/shop/*.html'");
    }

    private static class MutableConfig implements XisDistributedConfig {
        private final Map<String, String> frontletHosts = new HashMap<>();
        private final Map<String, String> frontletUrls = new HashMap<>();
        private final Map<String, String> pageHosts = new HashMap<>();

        @Override
        public Map<String, String> getFrontletHosts() {
            return frontletHosts;
        }

        @Override
        public Map<String, String> getFrontletUrls() {
            return frontletUrls;
        }

        @Override
        public Map<String, String> getPageHosts() {
            return pageHosts;
        }
    }

    private static class CountingConfig implements XisDistributedConfig {
        private int frontletHostCalls;
        private int frontletUrlCalls;
        private int pageHostCalls;

        @Override
        public Map<String, String> getFrontletHosts() {
            frontletHostCalls++;
            return Map.of("ScoreFrontlet", "https://frontlets.example.com");
        }

        @Override
        public Map<String, String> getFrontletUrls() {
            frontletUrlCalls++;
            return Map.of("ScoreFrontlet", "/score");
        }

        @Override
        public Map<String, String> getPageHosts() {
            pageHostCalls++;
            return Map.of("/shop/*.html", "https://pages.example.com");
        }
    }
}
