package one.xis.distributed;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DistributedComponentHostResolverTest {

    @Test
    void returnsConfiguredWidgetHost() {
        var resolver = new DistributedComponentHostResolver(new XisDistributedConfig() {
            @Override
            public boolean isRemoteWidget(String widgetId) {
                return "ScoreWidget".equals(widgetId);
            }

            @Override
            public boolean isRemotePage(String normalizedPath) {
                return false;
            }

            @Override
            public String getWidgetHost(String widgetId) {
                return "https://widgets.example.com";
            }

            @Override
            public String getPageHost(String normalizedPath) {
                return null;
            }
        });

        assertThat(resolver.getWidgetHost("ScoreWidget")).isEqualTo("https://widgets.example.com");
    }

    @Test
    void returnsConfiguredPageHost() {
        var resolver = new DistributedComponentHostResolver(new XisDistributedConfig() {
            @Override
            public boolean isRemoteWidget(String widgetId) {
                return false;
            }

            @Override
            public boolean isRemotePage(String normalizedPath) {
                return "/shop/*.html".equals(normalizedPath);
            }

            @Override
            public String getWidgetHost(String widgetId) {
                return null;
            }

            @Override
            public String getPageHost(String normalizedPath) {
                return "https://pages.example.com";
            }
        });

        assertThat(resolver.getPageHost("/shop/*.html")).isEqualTo("https://pages.example.com");
    }

    @Test
    void returnsNullForLocalComponent() {
        var resolver = new DistributedComponentHostResolver(new XisDistributedConfig() {
            @Override
            public boolean isRemoteWidget(String widgetId) {
                return false;
            }

            @Override
            public boolean isRemotePage(String normalizedPath) {
                return false;
            }

            @Override
            public String getWidgetHost(String widgetId) {
                return null;
            }

            @Override
            public String getPageHost(String normalizedPath) {
                return null;
            }
        });

        assertThat(resolver.getWidgetHost("LocalWidget")).isNull();
        assertThat(resolver.getPageHost("/local.html")).isNull();
    }

    @Test
    void throwsForRemoteWidgetWithoutHost() {
        var resolver = new DistributedComponentHostResolver(new XisDistributedConfig() {
            @Override
            public boolean isRemoteWidget(String widgetId) {
                return true;
            }

            @Override
            public boolean isRemotePage(String normalizedPath) {
                return false;
            }

            @Override
            public String getWidgetHost(String widgetId) {
                return null;
            }

            @Override
            public String getPageHost(String normalizedPath) {
                return null;
            }
        });

        assertThatThrownBy(() -> resolver.getWidgetHost("BrokenWidget"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BrokenWidget");
    }

    @Test
    void throwsForRemotePageWithoutHost() {
        var resolver = new DistributedComponentHostResolver(new XisDistributedConfig() {
            @Override
            public boolean isRemoteWidget(String widgetId) {
                return false;
            }

            @Override
            public boolean isRemotePage(String normalizedPath) {
                return true;
            }

            @Override
            public String getWidgetHost(String widgetId) {
                return null;
            }

            @Override
            public String getPageHost(String normalizedPath) {
                return " ";
            }
        });

        assertThatThrownBy(() -> resolver.getPageHost("/broken/*.html"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("/broken/*.html");
    }
}
