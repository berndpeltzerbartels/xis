package one.xis.server;

import java.util.Map;

/**
 * SPI for resolving host addresses in distributed / micro-frontend deployments.
 * <p>
 * When {@code xis-distributed} is on the classpath it contributes an implementation
 * of this interface. The server-side frontlet and page attribute factories
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
     * for the given frontlet-id, or {@code null} when the frontlet is intentionally local.
     */
    String getFrontletHost(String frontletId);

    /**
     * Returns known remote frontlet hosts by frontlet-id. This allows the client
     * configuration to contain remote frontlets even when their Java controller
     * classes are not on the local application classpath.
     */
    default Map<String, String> getFrontletHosts() {
        return Map.of();
    }

    /**
     * Returns known frontlet URLs by frontlet-id. The JavaScript client can use
     * these URLs as the public boundary for distributed frontlet navigation.
     */
    default Map<String, String> getFrontletUrls() {
        return Map.of();
    }

    /**
     * Returns the host for the given normalised page path (path-variables replaced
     * by {@code *}), or {@code null} when the page is intentionally local.
     */
    String getPageHost(String normalizedPath);
}
