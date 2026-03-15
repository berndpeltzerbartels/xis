package one.xis.server;

import one.xis.context.Component;
import one.xis.context.DefaultComponent;

/**
 * Default (no-op) implementation of {@link ComponentHostResolver}.
 * <p>
 * Active whenever {@code xis-distributed} is <em>not</em> on the classpath.
 * Always returns {@code null}, meaning every component is served from the same
 * origin as the main application – the JavaScript client uses relative URLs.
 * <p>
 * When {@code xis-distributed} is added as a dependency its
 * {@code DistributedComponentHostResolver} replaces this default.
 */
@Component
@DefaultComponent
class NoOpComponentHostResolver implements ComponentHostResolver {

    @Override
    public String getWidgetHost(String widgetId) {
        return null;
    }

    @Override
    public String getPageHost(String normalizedPath) {
        return null;
    }
}
