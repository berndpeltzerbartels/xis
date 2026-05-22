package one.xis.distributed;

import one.xis.ImportInstances;

import java.util.List;
import java.util.Set;

/**
 * Configuration interface for distributed / micro-frontend XIS deployments.
 * <p>
 * This interface models only the remote host topology of a distributed
 * application. The browser loads each remote host's normal XIS client config
 * and derives remote page/frontlet routing from that.
 * <p>
 * Implementations may perform expensive discovery while building the host list. They
 * should not be called once per routing decision by the framework.
 * <p>
 * {@link ImportInstances} is attached for internal framework integration so that
 * runtime-specific beans can be imported into the XIS context. It is not part of the
 * public application contract.
 */
@ImportInstances
public interface XisDistributedConfig {

    List<String> getHosts();

    /**
     * Returns browser origins that may call remote XIS endpoints in this
     * distributed application.
     * <p>
     * Implementations may return an empty set when no cross-origin calls are
     * expected. The properties-based implementation uses the same configured host
     * list for distributed config discovery and CORS.
     */
    default Set<String> getAllowedOrigins() {
        return Set.copyOf(getHosts());
    }
}
