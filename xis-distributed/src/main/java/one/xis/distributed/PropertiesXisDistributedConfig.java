package one.xis.distributed;

import lombok.extern.slf4j.Slf4j;
import one.xis.context.ApplicationProperties;
import one.xis.context.Component;
import one.xis.context.DefaultComponent;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link XisDistributedConfig} that reads distributed
 * hosts from {@code application.properties} (or a profile-specific variant).
 * <p>
 * Property format:
 * <pre>
 * xis.distributed.hosts=https://shop.example.com,https://catalog.example.com
 * </pre>
 * <p>
 * This bean is annotated {@link DefaultComponent}: if the application provides
 * its own {@link XisDistributedConfig} bean, this implementation is ignored.
 */
@Slf4j
@Component
@DefaultComponent
public class PropertiesXisDistributedConfig implements XisDistributedConfig {

    private static final String HOSTS_PROPERTY = "xis.distributed.hosts";

    private final List<String> hosts;

    public PropertiesXisDistributedConfig() {
        var value = ApplicationProperties.getProperty(HOSTS_PROPERTY);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing distributed host configuration '" + HOSTS_PROPERTY + "'.");
        }
        this.hosts = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(host -> !host.isEmpty())
                .map(this::normalizeHost)
                .distinct()
                .collect(Collectors.toList());
        if (hosts.isEmpty()) {
            throw new IllegalStateException("Distributed host configuration '" + HOSTS_PROPERTY + "' must contain at least one host.");
        }
        log.debug("Distributed config: hosts {}", hosts);
    }

    @Override
    public List<String> getHosts() {
        return List.copyOf(hosts);
    }

    private String normalizeHost(String value) {
        URI uri = URI.create(value);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();
        if (scheme == null || host == null) {
            throw new IllegalStateException("Distributed host must include scheme and host: " + value);
        }
        return scheme + "://" + host + (port >= 0 ? ":" + port : "");
    }
}
