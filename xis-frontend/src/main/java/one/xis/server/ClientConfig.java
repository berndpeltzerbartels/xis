package one.xis.server;


import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.Map;

/**
 * Every field is annotated to avoid micronaut ignores
 * empty arrays etc.
 */
@Data
@Builder
public class ClientConfig {

    private final Collection<String> widgetIds;
    private final Collection<String> pageIds;
    private boolean useWebsockets;

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


}
