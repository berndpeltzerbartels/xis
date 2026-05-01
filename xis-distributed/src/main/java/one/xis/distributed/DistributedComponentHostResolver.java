package one.xis.distributed;

import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.server.ComponentHostResolver;

/**
 * Bridges {@link XisDistributedConfig} to the framework-internal {@link ComponentHostResolver}.
 */
@Component
@RequiredArgsConstructor
public class DistributedComponentHostResolver implements ComponentHostResolver {

    private final XisDistributedConfig config;

    @Override
    public String getWidgetHost(String widgetId) {
        if (!config.isRemoteWidget(widgetId)) {
            return null;
        }
        String host = config.getWidgetHost(widgetId);
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("Missing remote host mapping for widget '" + widgetId + "'.");
        }
        return host;
    }

    @Override
    public String getPageHost(String normalizedPath) {
        if (!config.isRemotePage(normalizedPath)) {
            return null;
        }
        String host = config.getPageHost(normalizedPath);
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("Missing remote host mapping for page '" + normalizedPath + "'.");
        }
        return host;
    }
}
