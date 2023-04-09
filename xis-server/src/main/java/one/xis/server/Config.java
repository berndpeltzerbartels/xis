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
    private final Map<String, String> widgetHosts;
    private final Map<String, String> pageHosts;
    private final String welcomePageId;
    private final Map<String, ComponentAttributes> pageAttributes;
    private final Map<String, ComponentAttributes> widgetAttributes;


}
