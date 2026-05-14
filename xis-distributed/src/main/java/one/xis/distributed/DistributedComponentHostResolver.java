package one.xis.distributed;

import one.xis.context.Component;
import one.xis.server.ComponentHostResolver;

import java.util.Map;

/**
 * Bridges {@link XisDistributedConfig} to the framework-internal {@link ComponentHostResolver}.
 */
@Component
public class DistributedComponentHostResolver implements ComponentHostResolver {

    private final Map<String, String> frontletHosts;
    private final Map<String, String> frontletUrls;
    private final Map<String, String> pageHosts;

    public DistributedComponentHostResolver(XisDistributedConfig config) {
        this.frontletHosts = copyHostMap(config.getFrontletHosts(), "frontlet");
        this.frontletUrls = Map.copyOf(config.getFrontletUrls());
        this.pageHosts = copyHostMap(config.getPageHosts(), "page");
    }

    @Override
    public String getFrontletHost(String frontletId) {
        return frontletHosts.get(frontletId);
    }

    @Override
    public Map<String, String> getFrontletHosts() {
        return frontletHosts;
    }

    @Override
    public Map<String, String> getFrontletUrls() {
        return frontletUrls;
    }

    @Override
    public String getPageHost(String normalizedPath) {
        return pageHosts.get(normalizedPath);
    }

    private Map<String, String> copyHostMap(Map<String, String> hosts, String componentType) {
        hosts.forEach((key, host) -> {
            if (host == null || host.isBlank()) {
                throw new IllegalStateException("Missing remote host mapping for " + componentType + " '" + key + "'.");
            }
        });
        return Map.copyOf(hosts);
    }
}
