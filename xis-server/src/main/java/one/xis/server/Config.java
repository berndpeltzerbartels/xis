package one.xis.server;


import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.Map;

@Data
@Builder
public class Config {
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
