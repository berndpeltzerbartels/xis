package one.xis.distributed;

import one.xis.ImportInstances;

import java.util.Map;
import java.util.Set;

/**
 * Configuration interface for distributed / micro-frontend XIS deployments.
 * <p>
 * This interface models only the remote-routing topology of a distributed
 * application. Applications expose maps of known remote pages/frontlets. XIS
 * copies those maps when the distributed resolver starts and derives the
 * remote/local decisions from that copy.
 * <p>
 * Implementations may perform expensive discovery while building the maps. They
 * should not be called once per routing decision by the framework.
 * <p>
 * {@link ImportInstances} is attached for internal framework integration so that
 * runtime-specific beans can be imported into the XIS context. It is not part of the
 * public application contract.
 */
@ImportInstances
public interface XisDistributedConfig {

    /**
     * Returns known remote frontlet hosts by frontlet id. XIS copies this map when
     * the distributed resolver starts and derives remote-frontlet checks from it.
     */
    default Map<String, String> getFrontletHosts() {
        return Map.of();
    }

    /**
     * Returns optional public frontlet URLs by frontlet id.
     */
    default Map<String, String> getFrontletUrls() {
        return Map.of();
    }

    /**
     * Returns known remote page hosts by normalized page path.
     * <p>
     * The map key is the page URL as XIS stores it in the client configuration.
     * Path variables are normalized to {@code *}. For example, a page declared as
     * {@code @Page("/product/{id}/details.html")} is configured with the key
     * {@code "/product/&#42;/details.html"}.
     * <p>
     * XIS intentionally uses this URL-shaped id instead of the page class name.
     * Distributed applications should be coupled through public URLs, not through
     * Java class names that may change during refactoring in another application.
     * XIS copies this map when the distributed resolver starts and derives
     * remote-page checks from it.
     */
    default Map<String, String> getPageHosts() {
        return Map.of();
    }

    /**
     * Returns browser origins that may call remote XIS endpoints in this
     * distributed application.
     * <p>
     * Implementations may return an empty set when no cross-origin calls are
     * expected. The properties-based implementation derives this from its remote
     * host mappings and optional explicit origin mappings.
     */
    default Set<String> getAllowedOrigins() {
        return Set.of();
    }
}
