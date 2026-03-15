package one.xis.distributed;

import lombok.extern.slf4j.Slf4j;
import one.xis.context.ApplicationProperties;
import one.xis.context.Component;
import one.xis.context.DefaultComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link XisDistributedConfig} that reads host
 * mappings from {@code application.properties} (or a profile-specific variant).
 * <p>
 * Property format:
 * <pre>
 * # Mandatory: host of this server (used as fallback for all local components)
 * xis.host=https://app.example.com
 *
 * # Optional: explicit remote hosts per widget-id or normalised page path
 * xis.remote.widget.ProductWidget=https://shop.example.com
 * xis.remote.widget.CartWidget=https://shop.example.com
 * xis.remote.page./product/*.html=https://shop.example.com
 * </pre>
 * <p>
 * This bean is annotated {@link DefaultComponent}: if the application provides
 * its own {@link XisDistributedConfig} bean, this implementation is ignored.
 */
@Slf4j
@Component
@DefaultComponent
public class PropertiesXisDistributedConfig implements XisDistributedConfig {

    private static final String HOST_KEY = "xis.host";
    private static final String WIDGET_PREFIX = "xis.remote.widget.";
    private static final String PAGE_PREFIX = "xis.remote.page.";

    private final String defaultHost;
    private final Map<String, String> widgetHosts = new HashMap<>();
    private final Map<String, String> pageHosts = new HashMap<>();

    public PropertiesXisDistributedConfig() {
        var all = ApplicationProperties.getAllProperties();

        defaultHost = all.get(HOST_KEY);
        if (defaultHost == null || defaultHost.isBlank()) {
            throw new IllegalStateException(
                    "xis-distributed is on the classpath but 'xis.host' is not configured. " +
                            "Add 'xis.host=https://your-server.example.com' to application.properties.");
        }
        log.info("Distributed config: default host = {}", defaultHost);

        for (var entry : all.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.startsWith(WIDGET_PREFIX)) {
                String widgetId = key.substring(WIDGET_PREFIX.length());
                widgetHosts.put(widgetId, value);
                log.debug("Distributed config: widget '{}' → {}", widgetId, value);
            } else if (key.startsWith(PAGE_PREFIX)) {
                String normalizedPath = key.substring(PAGE_PREFIX.length());
                pageHosts.put(normalizedPath, value);
                log.debug("Distributed config: page '{}' → {}", normalizedPath, value);
            }
        }
    }

    @Override
    public String getWidgetHost(String widgetId) {
        return widgetHosts.getOrDefault(widgetId, defaultHost);
    }

    @Override
    public String getPageHost(String normalizedPath) {
        return pageHosts.getOrDefault(normalizedPath, defaultHost);
    }
}
