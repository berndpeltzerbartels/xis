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
        return config.getWidgetHost(widgetId);
    }

    @Override
    public String getPageHost(String normalizedPath) {
        return config.getPageHost(normalizedPath);
    }
}
