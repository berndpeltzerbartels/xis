package one.xis.server;

/**
 * SPI for resolving host addresses in distributed / micro-frontend deployments.
 * <p>
 * When {@code xis-distributed} is on the classpath it contributes an implementation
 * of this interface. {@link FrontletAttributesFactory} and {@link PageAttributesFactory}
 * use it to fill the {@code host} field so the JavaScript client knows which server
 * to contact for each component.
 * <p>
 * The distributed implementation returns a non-null host only for components that
 * are intentionally routed to a remote server. Components that remain local return
 * {@code null}, causing the browser to use same-origin relative URLs.
 */
public interface ComponentHostResolver {

    /**
     * Returns the host (scheme + authority, e.g. {@code https://shop.example.com})
     * for the given widget-id, or {@code null} when the widget is intentionally local.
     */
    String getWidgetHost(String widgetId);

    /**
     * Returns the host for the given normalised page path (path-variables replaced
     * by {@code *}), or {@code null} when the page is intentionally local.
     */
    String getPageHost(String normalizedPath);
}
