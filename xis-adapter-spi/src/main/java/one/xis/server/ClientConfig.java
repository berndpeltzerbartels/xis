package one.xis.server;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Every field is annotated to avoid micronaut ignores
 * empty arrays etc.
 */
@Data
@AllArgsConstructor
public class ClientConfig {

    private final Collection<String> widgetIds;
    private final Collection<String> pageIds;
    private final Collection<String> includeIds;
    private final boolean useWebsockets; // TODO remove this, use websockets only if configured in server config
    /**
     * Normalized path of the welcome-page.
     */
    private final String welcomePageId;

    /**
     * Page attributes by normalized path. Must contain all pages.
     */
    private final Map<String, PageAttributes> pageAttributes;

    /**
     * Hosts by widget-id. Must contain all widgets.
     */
    private final Map<String, WidgetAttributes> widgetAttributes;

    static ClientConfig.ClientConfigBuilder builder() {
        return new ClientConfig.ClientConfigBuilder();
    }


    static class ClientConfigBuilder {
        private Collection<String> widgetIds = List.of();
        private Collection<String> pageIds = List.of();
        private Collection<String> includeIds = List.of();
        private boolean useWebsockets = false;

        @Getter
        private String welcomePageId;
        private Map<String, PageAttributes> pageAttributes = Map.of();
        private Map<String, WidgetAttributes> widgetAttributes = Map.of();

        ClientConfigBuilder widgetIds(Collection<String> widgetIds) {
            this.widgetIds = widgetIds;
            return this;
        }

        ClientConfigBuilder pageIds(Collection<String> pageIds) {
            this.pageIds = pageIds;
            return this;
        }

        ClientConfigBuilder includeIds(Collection<String> includeIds) {
            this.includeIds = includeIds;
            return this;
        }

        ClientConfigBuilder useWebsockets(boolean useWebsockets) {
            this.useWebsockets = useWebsockets;
            return this;
        }

        ClientConfigBuilder welcomePageId(String welcomePageId) {
            this.welcomePageId = welcomePageId;
            return this;
        }

        ClientConfigBuilder pageAttributes(Map<String, PageAttributes> pageAttributes) {
            this.pageAttributes = pageAttributes;
            return this;
        }

        ClientConfigBuilder widgetAttributes(Map<String, WidgetAttributes> widgetAttributes) {
            this.widgetAttributes = widgetAttributes;
            return this;
        }
        
        ClientConfig build() {
            return new ClientConfig(
                    widgetIds,
                    pageIds,
                    includeIds,
                    useWebsockets,
                    welcomePageId,
                    pageAttributes,
                    widgetAttributes
            );
        }
    }


}
