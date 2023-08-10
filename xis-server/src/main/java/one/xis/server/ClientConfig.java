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

    /**
     * Normalized path of the welcome-page.
     */
    private final String welcomePageId;

    /**
     * Key is the normalized path
     *
     * @{@link Path}
     */
    private final Map<String, PageAttributes> pageAttributes;

    /**
     * Key is the normalized path
     *
     * @{@link Path}
     */
    private final Map<String, ComponentAttributes> widgetAttributes;


}
