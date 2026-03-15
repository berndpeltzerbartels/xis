package one.xis.server;

/**
 * SPI for resolving host addresses in distributed / micro-frontend deployments.
 * <p>
 * When {@code xis-distributed} is on the classpath it contributes an implementation
 * of this interface. {@link WidgetAttributesFactory} and {@link PageAttributesFactory}
 * use it to fill the {@code host} field so the JavaScript client knows which server
 * to contact for each component.
 * <p>
 * Every method always returns a non-null host string.  The default implementation
 * falls back to {@code xis.host} for components that have no explicit mapping.
 * If the module is absent no implementation is registered and the factories leave
 * {@code host} as {@code null} (= same-origin).
 */
public interface ComponentHostResolver {

    /**
     * Returns the host (scheme + authority, e.g. {@code https://shop.example.com})
     * for the given widget-id.  Falls back to the configured default host if no
     * explicit mapping exists.
     */
    String getWidgetHost(String widgetId);

    /**
     * Returns the host for the given normalised page path (path-variables replaced
     * by {@code *}).  Falls back to the configured default host if no explicit
     * mapping exists.
     */
    String getPageHost(String normalizedPath);
}
