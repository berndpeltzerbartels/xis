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

    private static final String WIDGET_PREFIX = "xis.remote.widget.";
    private static final String PAGE_PREFIX = "xis.remote.page.";

    private final Map<String, String> widgetHosts = new HashMap<>();
    private final Map<String, String> pageHosts = new HashMap<>();

    public PropertiesXisDistributedConfig() {
        var all = ApplicationProperties.getAllProperties();

        for (var entry : all.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.startsWith(WIDGET_PREFIX)) {
                String widgetId = key.substring(WIDGET_PREFIX.length());
                validateHostValue(key, value);
                widgetHosts.put(widgetId, value);
                log.debug("Distributed config: widget '{}' → {}", widgetId, value);
            } else if (key.startsWith(PAGE_PREFIX)) {
                String normalizedPath = key.substring(PAGE_PREFIX.length());
                validateHostValue(key, value);
                pageHosts.put(normalizedPath, value);
                log.debug("Distributed config: page '{}' → {}", normalizedPath, value);
            }
        }
    }

    private void validateHostValue(String key, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Distributed host mapping '" + key + "' must not be blank.");
        }
    }

    @Override
    public boolean isRemoteWidget(String widgetId) {
        return widgetHosts.containsKey(widgetId);
    }

    @Override
    public boolean isRemotePage(String normalizedPath) {
        return pageHosts.containsKey(normalizedPath);
    }

    @Override
    public String getWidgetHost(String widgetId) {
        return widgetHosts.get(widgetId);
    }

    @Override
    public String getPageHost(String normalizedPath) {
        return pageHosts.get(normalizedPath);
    }
}
